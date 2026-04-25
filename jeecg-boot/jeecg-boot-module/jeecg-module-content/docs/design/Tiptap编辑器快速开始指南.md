# Tiptap编辑器快速开始指南

## 🚀 快速开始

本指南将帮助您在30分钟内快速搭建一个基本的Tiptap编辑器功能。

### 📋 前置条件

- JeecgBoot项目环境已搭建完成
- Node.js 16+ 和 npm/yarn
- MySQL 8.0+
- Redis（可选，用于缓存）

## 技术栈

- **后端**: JeecgBoot + SpringBoot + MyBatis-Plus
- **前端**: Vue 3 + TypeScript + Element Plus
- **编辑器**: **Tiptap 3.x** + 多种扩展（采用新的命名导入方式）
- **数据库**: MySQL 8.0+
- **缓存**: Redis

## 🎯 第一步：数据库准备（5分钟）

### 1.1 执行数据库脚本

在MySQL中执行以下SQL创建基础表：

```sql
-- 编辑器内容表（简化版）
CREATE TABLE editor_content (
    id VARCHAR(32) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    html_content LONGTEXT,
    status INTEGER DEFAULT 0,
    author_id VARCHAR(32),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    del_flag INTEGER DEFAULT 0
);
```

## 🔧 第二步：后端快速实现（10分钟）

### 2.1 创建实体类

在 `jeecg-module-content/src/main/java/org/jeecg/modules/content/entity/` 创建：

```java
// EditorContent.java
@Data
@TableName("editor_content")
@ApiModel("编辑器内容")
public class EditorContent implements Serializable {
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    
    @ApiModelProperty("标题")
    private String title;
    
    @ApiModelProperty("内容JSON")
    private String content;
    
    @ApiModelProperty("HTML内容")
    private String htmlContent;
    
    @ApiModelProperty("状态")
    private Integer status;
    
    @ApiModelProperty("作者ID")
    private String authorId;
    
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    @TableLogic
    private Integer delFlag;
}
```

### 2.2 创建Mapper接口

```java
// EditorContentMapper.java
@Mapper
public interface EditorContentMapper extends BaseMapper<EditorContent> {
}
```

### 2.3 创建Service

```java
// IEditorContentService.java
public interface IEditorContentService extends IService<EditorContent> {
    String saveContent(String title, String content, String htmlContent);
    EditorContent getContentById(String id);
}

// EditorContentServiceImpl.java
@Service
public class EditorContentServiceImpl extends ServiceImpl<EditorContentMapper, EditorContent> 
    implements IEditorContentService {
    
    @Override
    public String saveContent(String title, String content, String htmlContent) {
        EditorContent entity = new EditorContent();
        entity.setTitle(title);
        entity.setContent(content);
        entity.setHtmlContent(htmlContent);
        entity.setStatus(0); // 草稿状态
        this.save(entity);
        return entity.getId();
    }
    
    @Override
    public EditorContent getContentById(String id) {
        return this.getById(id);
    }
}
```

### 2.4 创建Controller

```java
// EditorController.java
@RestController
@RequestMapping("/content/editor")
@Api(tags = "编辑器管理")
public class EditorController {
    
    @Autowired
    private IEditorContentService editorContentService;
    
    @PostMapping("/save")
    @ApiOperation("保存内容")
    public Result<String> saveContent(@RequestBody Map<String, String> params) {
        String title = params.get("title");
        String content = params.get("content");
        String htmlContent = params.get("htmlContent");
        
        String id = editorContentService.saveContent(title, content, htmlContent);
        return Result.OK("保存成功", id);
    }
    
    @GetMapping("/get/{id}")
    @ApiOperation("获取内容")
    public Result<EditorContent> getContent(@PathVariable String id) {
        EditorContent content = editorContentService.getContentById(id);
        return Result.OK(content);
    }
}
```

## 🎨 第三步：前端快速实现（15分钟）

### 3.1 安装Tiptap V3依赖

在 `jeecgboot-vue3` 目录下执行：

```bash
# 安装Tiptap 3.x核心包（注意V3需要额外安装@tiptap/core）
npm install @tiptap/core @tiptap/vue-3 @tiptap/starter-kit @tiptap/extension-image @tiptap/extension-link

# 安装其他常用扩展
npm install @tiptap/extension-table @tiptap/extension-code-block-lowlight
```

### 3.2 创建基础编辑器组件

在 `src/components/` 创建 `TiptapEditor/index.vue`：

```vue
<template>
  <div class="tiptap-editor">
    <!-- 简单工具栏 -->
    <div class="toolbar">
      <el-button-group>
        <el-button 
          :class="{ 'is-active': editor?.isActive('bold') }"
          @click="editor?.chain().focus().toggleBold().run()"
          size="small"
        >
          <el-icon><Bold /></el-icon>
        </el-button>
        <el-button 
          :class="{ 'is-active': editor?.isActive('italic') }"
          @click="editor?.chain().focus().toggleItalic().run()"
          size="small"
        >
          <el-icon><Italic /></el-icon>
        </el-button>
        <el-button 
          @click="editor?.chain().focus().toggleHeading({ level: 1 }).run()"
          size="small"
        >
          H1
        </el-button>
        <el-button 
          @click="editor?.chain().focus().toggleBulletList().run()"
          size="small"
        >
          <el-icon><List /></el-icon>
        </el-button>
      </el-button-group>
      
      <el-button @click="saveContent" type="primary" size="small">
        保存
      </el-button>
    </div>
    
    <!-- 编辑器 -->
    <EditorContent :editor="editor" class="editor-content" />
  </div>
</template>

<script setup lang="ts">
import { Editor, EditorContent } from '@tiptap/vue-3'
import { StarterKit } from '@tiptap/starter-kit'
import { Image } from '@tiptap/extension-image'
import { Link } from '@tiptap/extension-link'
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { saveEditorContent } from '@/api/content/editor'

interface Props {
  modelValue?: string
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  title: ''
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const editor = ref<Editor | null>(null)

// 初始化编辑器
const initEditor = () => {
  editor.value = new Editor({
    extensions: [
      StarterKit,
      Image.configure({
        inline: true,
        allowBase64: true,
      }),
      Link.configure({
        openOnClick: false,
      }),
    ],
    content: props.modelValue,
    onUpdate: ({ editor }) => {
      const json = editor.getJSON()
      emit('update:modelValue', JSON.stringify(json))
    },
  })
}

// 保存内容
const saveContent = async () => {
  if (!editor.value) return
  
  try {
    const content = JSON.stringify(editor.value.getJSON())
    const htmlContent = editor.value.getHTML()
    
    await saveEditorContent({
      title: props.title || '未命名文档',
      content,
      htmlContent
    })
    
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

onMounted(() => {
  initEditor()
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>

<style scoped>
.tiptap-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.toolbar {
  padding: 10px;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.editor-content {
  padding: 20px;
  min-height: 400px;
}

.editor-content :deep(.ProseMirror) {
  outline: none;
  line-height: 1.6;
}

.editor-content :deep(.ProseMirror h1) {
  font-size: 2em;
  font-weight: bold;
  margin: 0.5em 0;
}

.editor-content :deep(.ProseMirror h2) {
  font-size: 1.5em;
  font-weight: bold;
  margin: 0.5em 0;
}

.editor-content :deep(.ProseMirror ul, .ProseMirror ol) {
  padding-left: 1.5em;
}

.editor-content :deep(.ProseMirror p) {
  margin: 0.5em 0;
}

.is-active {
  background-color: #409eff;
  color: white;
}
</style>
```

### 3.3 创建API接口

在 `src/api/content/` 创建 `editor.ts`：

```typescript
import { defHttp } from '@/utils/http/axios'

const API = {
  SAVE_CONTENT: '/content/editor/save',
  GET_CONTENT: '/content/editor/get',
}

/**
 * 保存编辑器内容
 */
export const saveEditorContent = (params: {
  title: string
  content: string
  htmlContent: string
}) => {
  return defHttp.post({ url: API.SAVE_CONTENT, params })
}

/**
 * 获取编辑器内容
 */
export const getEditorContent = (id: string) => {
  return defHttp.get({ url: `${API.GET_CONTENT}/${id}` })
}
```

### 3.4 创建使用页面

在 `src/views/content/` 创建 `EditorDemo.vue`：

```vue
<template>
  <div class="editor-demo">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>Tiptap编辑器演示</span>
        </div>
      </template>
      
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        
        <el-form-item label="内容">
          <TiptapEditor 
            v-model="form.content" 
            :title="form.title"
          />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import TiptapEditor from '@/components/TiptapEditor/index.vue'

const form = reactive({
  title: '',
  content: ''
})
</script>

<style scoped>
.editor-demo {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

### 3.5 添加路由

在路由配置中添加：

```typescript
{
  path: '/content/editor-demo',
  name: 'EditorDemo',
  component: () => import('@/views/content/EditorDemo.vue'),
  meta: {
    title: '编辑器演示',
    keepAlive: false,
  }
}
```

## 🧪 第四步：测试验证

### 4.1 启动项目

1. 启动后端服务
2. 启动前端服务
3. 访问编辑器演示页面

### 4.2 功能测试

- [ ] 文本编辑（粗体、斜体）
- [ ] 标题设置
- [ ] 列表创建
- [ ] 内容保存
- [ ] 数据持久化

## 🚀 下一步扩展

完成基础功能后，可以继续添加：

### 高级功能
- 图片上传
- 表格编辑
- 代码高亮
- Markdown支持
- 协作编辑

### 性能优化
- 自动保存
- 内容缓存
- 懒加载
- 虚拟滚动

### 用户体验
- 快捷键支持
- 主题切换
- 全屏编辑
- 预览模式

## 📚 参考资源

- [Tiptap官方文档](https://tiptap.dev/)
- [JeecgBoot文档](http://doc.jeecg.com/)
- [Element Plus文档](https://element-plus.org/)
- [Vue 3文档](https://vuejs.org/)

## ❓ 常见问题

### Q: 编辑器不显示？
A: 检查依赖是否正确安装，确保导入路径正确。

### Q: 保存失败？
A: 检查后端接口是否正常，数据库连接是否正常。

### Q: 样式异常？
A: 检查CSS样式是否正确加载，可能需要调整样式优先级。

### Q: 扩展不生效？
A: 确保扩展正确导入和配置，检查版本兼容性。

---

🎉 **恭喜！** 您已经成功搭建了一个基础的Tiptap编辑器。现在可以根据需求继续扩展更多功能。

如需更详细的实现指南，请参考：
- [Tiptap编辑器功能设计与实现文档.md](./Tiptap编辑器功能设计与实现文档.md)
- [AI智能体实现指南-Tiptap编辑器.md](./AI智能体实现指南-Tiptap编辑器.md)