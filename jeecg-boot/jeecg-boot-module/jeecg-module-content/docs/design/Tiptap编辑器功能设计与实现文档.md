# Tiptap编辑器功能设计与实现文档

## 1. 功能概述

### 1.1 功能描述
基于Tiptap构建一个功能强大的富文本编辑器，支持Markdown语法、多种扩展插件，为内容创作提供优秀的用户体验。

### 1.2 核心特性
- 基于Tiptap V3构建的现代化富文本编辑器
- 完整的Markdown语法支持
- 丰富的扩展插件生态
- 响应式设计，支持移动端
- 实时协作编辑能力
- 自定义主题和样式
- 内容导入导出功能

## 2. 技术架构设计

### 2.1 技术栈选择

#### 前端技术栈
- **Tiptap v3**: 核心编辑器框架（最新版本）
- **Vue 3**: 前端框架（与JeecgBoot Vue3版本保持一致）
- **TypeScript**: 类型安全的JavaScript超集
- **Vite**: 构建工具
- **Element Plus**: UI组件库（与项目保持一致）
- **@tiptap/extension-***: Tiptap官方扩展包

#### 后端技术栈
- **Spring Boot**: 后端框架
- **MyBatis-Plus**: ORM框架
- **Redis**: 缓存和会话存储
- **MinIO**: 文件存储服务
- **WebSocket**: 实时协作支持

### 2.2 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                    前端架构层次                              │
├─────────────────────────────────────────────────────────────┤
│  展示层 (Presentation Layer)                               │
│  ├── 编辑器组件 (TiptapEditor.vue)                         │
│  ├── 工具栏组件 (EditorToolbar.vue)                        │
│  ├── 侧边栏组件 (EditorSidebar.vue)                        │
│  └── 预览组件 (EditorPreview.vue)                          │
├─────────────────────────────────────────────────────────────┤
│  业务逻辑层 (Business Logic Layer)                         │
│  ├── 编辑器服务 (EditorService.ts)                         │
│  ├── 文件服务 (FileService.ts)                             │
│  ├── 协作服务 (CollaborationService.ts)                    │
│  └── 主题服务 (ThemeService.ts)                            │
├─────────────────────────────────────────────────────────────┤
│  数据访问层 (Data Access Layer)                            │
│  ├── API接口 (api/editor.ts)                               │
│  ├── WebSocket连接 (websocket/collaboration.ts)            │
│  └── 本地存储 (storage/localStorage.ts)                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    后端架构层次                              │
├─────────────────────────────────────────────────────────────┤
│  控制层 (Controller Layer)                                 │
│  ├── EditorController                                      │
│  ├── FileUploadController                                  │
│  └── CollaborationController                               │
├─────────────────────────────────────────────────────────────┤
│  业务逻辑层 (Service Layer)                                │
│  ├── IEditorService / EditorServiceImpl                    │
│  ├── IFileService / FileServiceImpl                        │
│  └── ICollaborationService / CollaborationServiceImpl      │
├─────────────────────────────────────────────────────────────┤
│  数据访问层 (Mapper Layer)                                 │
│  ├── EditorContentMapper                                   │
│  ├── EditorTemplateMapper                                  │
│  └── EditorHistoryMapper                                   │
├─────────────────────────────────────────────────────────────┤
│  实体层 (Entity Layer)                                     │
│  ├── EditorContent                                         │
│  ├── EditorTemplate                                        │
│  └── EditorHistory                                         │
└─────────────────────────────────────────────────────────────┘
```

## 3. 数据库设计

### 3.1 数据表设计

#### 编辑器内容表 (editor_content)
```sql
CREATE TABLE editor_content (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(255) NOT NULL COMMENT '内容标题',
    content LONGTEXT COMMENT '编辑器内容(JSON格式)',
    markdown_content LONGTEXT COMMENT 'Markdown格式内容',
    html_content LONGTEXT COMMENT 'HTML格式内容',
    content_type VARCHAR(50) DEFAULT 'article' COMMENT '内容类型',
    status INTEGER DEFAULT 0 COMMENT '状态(0:草稿,1:发布,2:归档)',
    tags VARCHAR(500) COMMENT '标签',
    category_id VARCHAR(32) COMMENT '分类ID',
    author_id VARCHAR(32) NOT NULL COMMENT '作者ID',
    word_count INTEGER DEFAULT 0 COMMENT '字数统计',
    reading_time INTEGER DEFAULT 0 COMMENT '预计阅读时间(分钟)',
    is_template INTEGER DEFAULT 0 COMMENT '是否为模板(0:否,1:是)',
    template_name VARCHAR(255) COMMENT '模板名称',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志(0:正常,1:删除)',
    INDEX idx_author_id (author_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT='编辑器内容表';
```

#### 编辑器历史记录表 (editor_history)
```sql
CREATE TABLE editor_history (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    content_id VARCHAR(32) NOT NULL COMMENT '内容ID',
    content LONGTEXT COMMENT '历史内容(JSON格式)',
    version_number INTEGER NOT NULL COMMENT '版本号',
    change_description VARCHAR(500) COMMENT '变更描述',
    operation_type VARCHAR(50) COMMENT '操作类型(create,update,delete)',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_content_id (content_id),
    INDEX idx_version_number (version_number),
    INDEX idx_create_time (create_time)
) COMMENT='编辑器历史记录表';
```

#### 编辑器模板表 (editor_template)
```sql
CREATE TABLE editor_template (
    id VARCHAR(32) PRIMARY KEY COMMENT '主键ID',
    template_name VARCHAR(255) NOT NULL COMMENT '模板名称',
    template_description VARCHAR(500) COMMENT '模板描述',
    template_content LONGTEXT COMMENT '模板内容(JSON格式)',
    template_preview TEXT COMMENT '模板预览图',
    category VARCHAR(100) COMMENT '模板分类',
    is_public INTEGER DEFAULT 0 COMMENT '是否公开(0:私有,1:公开)',
    use_count INTEGER DEFAULT 0 COMMENT '使用次数',
    create_by VARCHAR(50) COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(50) COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag INTEGER DEFAULT 0 COMMENT '删除标志(0:正常,1:删除)',
    INDEX idx_category (category),
    INDEX idx_is_public (is_public),
    INDEX idx_create_time (create_time)
) COMMENT='编辑器模板表';
```

## 4. 前端实现方案

### 4.1 核心组件设计

#### TiptapEditor.vue - 主编辑器组件
```typescript
<template>
  <div class="tiptap-editor-container">
    <!-- 工具栏 -->
    <EditorToolbar 
      :editor="editor" 
      @upload-image="handleImageUpload"
      @insert-table="handleInsertTable"
    />
    
    <!-- 编辑器主体 -->
    <div class="editor-wrapper">
      <EditorContent 
        :editor="editor" 
        class="editor-content"
      />
    </div>
    
    <!-- 状态栏 -->
    <div class="editor-status-bar">
      <span>字数: {{ wordCount }}</span>
      <span>字符: {{ characterCount }}</span>
      <span>预计阅读: {{ readingTime }}分钟</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Editor, EditorContent } from '@tiptap/vue-3'
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useEditorExtensions } from '@/composables/useEditorExtensions'
import { useEditorContent } from '@/composables/useEditorContent'
import EditorToolbar from './EditorToolbar.vue'

interface Props {
  modelValue?: string
  placeholder?: string
  editable?: boolean
  autofocus?: boolean
  extensions?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  placeholder: '开始写作...',
  editable: true,
  autofocus: false,
  extensions: () => []
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'change': [content: any]
  'focus': []
  'blur': []
}>()

// 获取编辑器扩展
const { getExtensions } = useEditorExtensions()

// 编辑器实例
const editor = ref<Editor | null>(null)

// 内容管理
const { saveContent, loadContent } = useEditorContent()

// 统计信息
const wordCount = computed(() => {
  return editor.value?.storage.characterCount?.words() || 0
})

const characterCount = computed(() => {
  return editor.value?.storage.characterCount?.characters() || 0
})

const readingTime = computed(() => {
  return Math.ceil(wordCount.value / 200) // 假设每分钟阅读200字
})

/**
 * 初始化编辑器
 */
const initEditor = () => {
  editor.value = new Editor({
    extensions: [
      ...getExtensions(),
      ...props.extensions
    ],
    content: props.modelValue,
    editable: props.editable,
    autofocus: props.autofocus,
    onUpdate: ({ editor }) => {
      const content = editor.getJSON()
      emit('update:modelValue', JSON.stringify(content))
      emit('change', content)
    },
    onFocus: () => emit('focus'),
    onBlur: () => emit('blur')
  })
}

/**
 * 处理图片上传
 */
const handleImageUpload = async (file: File) => {
  try {
    // 调用文件上传服务
    const url = await uploadFile(file)
    editor.value?.chain().focus().setImage({ src: url }).run()
  } catch (error) {
    console.error('图片上传失败:', error)
  }
}

/**
 * 处理表格插入
 */
const handleInsertTable = (rows: number, cols: number) => {
  editor.value?.chain().focus().insertTable({ 
    rows, 
    cols, 
    withHeaderRow: true 
  }).run()
}

onMounted(() => {
  initEditor()
})

onBeforeUnmount(() => {
  editor.value?.destroy()
})
</script>
```

### 4.2 编辑器扩展配置

#### useEditorExtensions.ts - 扩展管理
```typescript
import { Extension } from '@tiptap/core'
import { StarterKit } from '@tiptap/starter-kit'
import { Image } from '@tiptap/extension-image'
import { Link } from '@tiptap/extension-link'
import { Table } from '@tiptap/extension-table'
import { TableRow } from '@tiptap/extension-table-row'
import { TableHeader } from '@tiptap/extension-table-header'
import { TableCell } from '@tiptap/extension-table-cell'
import { CodeBlockLowlight } from '@tiptap/extension-code-block-lowlight'
import { createLowlight } from 'lowlight'
import { Highlight } from '@tiptap/extension-highlight'
import { Typography } from '@tiptap/extension-typography'
import { Placeholder } from '@tiptap/extension-placeholder'
import { CharacterCount } from '@tiptap/extension-character-count'
import { TaskList } from '@tiptap/extension-task-list'
import { TaskItem } from '@tiptap/extension-task-item'
import { Mention } from '@tiptap/extension-mention'
import { Markdown } from 'tiptap-markdown'

/**
 * 编辑器扩展管理组合式函数
 */
export function useEditorExtensions() {
  
  /**
   * 获取基础扩展列表
   * @returns Extension[] 扩展数组
   */
  const getExtensions = (): Extension[] => {
    // 创建lowlight实例
    const lowlight = createLowlight()
    
    return [
      // 基础扩展包
      StarterKit.configure({
        codeBlock: false, // 禁用默认代码块，使用增强版
      }),
      
      // Markdown支持
      Markdown.configure({
        html: true,
        tightLists: true,
        tightListClass: 'tight',
        bulletListMarker: '-',
        linkify: true,
        breaks: false,
        transformPastedText: true,
        transformCopiedText: true,
      }),
      
      // 图片扩展
      Image.configure({
        inline: true,
        allowBase64: true,
        HTMLAttributes: {
          class: 'editor-image',
        },
      }),
      
      // 链接扩展
      Link.configure({
        openOnClick: false,
        HTMLAttributes: {
          class: 'editor-link',
          rel: 'noopener noreferrer',
          target: '_blank',
        },
      }),
      
      // 表格扩展
      Table.configure({
        resizable: true,
        HTMLAttributes: {
          class: 'editor-table',
        },
      }),
      TableRow,
      TableHeader,
      TableCell,
      
      // 代码高亮
      CodeBlockLowlight.configure({
        lowlight,
        HTMLAttributes: {
          class: 'editor-code-block',
        },
      }),
      
      // 文本高亮
      Highlight.configure({
        multicolor: true,
        HTMLAttributes: {
          class: 'editor-highlight',
        },
      }),
      
      // 排版增强
      Typography,
      
      // 占位符
      Placeholder.configure({
        placeholder: '开始写作...',
        emptyEditorClass: 'is-editor-empty',
      }),
      
      // 字符统计
      CharacterCount,
      
      // 任务列表
      TaskList.configure({
        HTMLAttributes: {
          class: 'task-list',
        },
      }),
      TaskItem.configure({
        nested: true,
        HTMLAttributes: {
          class: 'task-item',
        },
      }),
      
      // 提及功能
      Mention.configure({
        HTMLAttributes: {
          class: 'mention',
        },
        suggestion: {
          items: ({ query }) => {
            // 这里可以接入用户搜索API
            return [
              { id: 1, label: '@张三' },
              { id: 2, label: '@李四' },
            ].filter(item => 
              item.label.toLowerCase().includes(query.toLowerCase())
            )
          },
        },
      }),
    ]
  }
  
  /**
   * 获取协作扩展
   */
  const getCollaborationExtensions = () => {
    // 这里可以添加协作相关的扩展
    return []
  }
  
  return {
    getExtensions,
    getCollaborationExtensions
  }
}
```

### 4.3 工具栏组件

#### EditorToolbar.vue - 编辑器工具栏
```typescript
<template>
  <div class="editor-toolbar">
    <!-- 格式化工具 -->
    <div class="toolbar-group">
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
          :class="{ 'is-active': editor?.isActive('strike') }"
          @click="editor?.chain().focus().toggleStrike().run()"
          size="small"
        >
          <el-icon><Strikethrough /></el-icon>
        </el-button>
      </el-button-group>
    </div>
    
    <!-- 标题工具 -->
    <div class="toolbar-group">
      <el-dropdown @command="setHeading">
        <el-button size="small">
          标题 <el-icon><ArrowDown /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="paragraph">正文</el-dropdown-item>
            <el-dropdown-item command="1">标题 1</el-dropdown-item>
            <el-dropdown-item command="2">标题 2</el-dropdown-item>
            <el-dropdown-item command="3">标题 3</el-dropdown-item>
            <el-dropdown-item command="4">标题 4</el-dropdown-item>
            <el-dropdown-item command="5">标题 5</el-dropdown-item>
            <el-dropdown-item command="6">标题 6</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <!-- 列表工具 -->
    <div class="toolbar-group">
      <el-button-group>
        <el-button 
          :class="{ 'is-active': editor?.isActive('bulletList') }"
          @click="editor?.chain().focus().toggleBulletList().run()"
          size="small"
        >
          <el-icon><List /></el-icon>
        </el-button>
        <el-button 
          :class="{ 'is-active': editor?.isActive('orderedList') }"
          @click="editor?.chain().focus().toggleOrderedList().run()"
          size="small"
        >
          <el-icon><Numbered /></el-icon>
        </el-button>
        <el-button 
          :class="{ 'is-active': editor?.isActive('taskList') }"
          @click="editor?.chain().focus().toggleTaskList().run()"
          size="small"
        >
          <el-icon><Check /></el-icon>
        </el-button>
      </el-button-group>
    </div>
    
    <!-- 插入工具 -->
    <div class="toolbar-group">
      <el-button-group>
        <el-button @click="insertImage" size="small">
          <el-icon><Picture /></el-icon>
        </el-button>
        <el-button @click="insertLink" size="small">
          <el-icon><Link /></el-icon>
        </el-button>
        <el-button @click="insertTable" size="small">
          <el-icon><Grid /></el-icon>
        </el-button>
        <el-button @click="insertCodeBlock" size="small">
          <el-icon><Code /></el-icon>
        </el-button>
      </el-button-group>
    </div>
    
    <!-- 更多工具 -->
    <div class="toolbar-group">
      <el-button-group>
        <el-button @click="undo" size="small" :disabled="!editor?.can().undo()">
          <el-icon><RefreshLeft /></el-icon>
        </el-button>
        <el-button @click="redo" size="small" :disabled="!editor?.can().redo()">
          <el-icon><RefreshRight /></el-icon>
        </el-button>
      </el-button-group>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Editor } from '@tiptap/vue-3'
import { ElMessage, ElMessageBox } from 'element-plus'

interface Props {
  editor: Editor | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'upload-image': [file: File]
  'insert-table': [rows: number, cols: number]
}>()

/**
 * 设置标题级别
 */
const setHeading = (level: string) => {
  if (level === 'paragraph') {
    props.editor?.chain().focus().setParagraph().run()
  } else {
    props.editor?.chain().focus().toggleHeading({ level: parseInt(level) as any }).run()
  }
}

/**
 * 插入图片
 */
const insertImage = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e) => {
    const file = (e.target as HTMLInputElement).files?.[0]
    if (file) {
      emit('upload-image', file)
    }
  }
  input.click()
}

/**
 * 插入链接
 */
const insertLink = async () => {
  try {
    const { value: url } = await ElMessageBox.prompt('请输入链接地址', '插入链接', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^https?:\/\/.+/,
      inputErrorMessage: '请输入有效的URL地址'
    })
    
    if (url) {
      props.editor?.chain().focus().setLink({ href: url }).run()
    }
  } catch {
    // 用户取消
  }
}

/**
 * 插入表格
 */
const insertTable = async () => {
  try {
    const { value } = await ElMessageBox.prompt('请输入表格尺寸（格式：行数x列数，如：3x4）', '插入表格', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^\d+x\d+$/,
      inputErrorMessage: '请输入正确的格式，如：3x4'
    })
    
    if (value) {
      const [rows, cols] = value.split('x').map(Number)
      emit('insert-table', rows, cols)
    }
  } catch {
    // 用户取消
  }
}

/**
 * 插入代码块
 */
const insertCodeBlock = () => {
  props.editor?.chain().focus().toggleCodeBlock().run()
}

/**
 * 撤销
 */
const undo = () => {
  props.editor?.chain().focus().undo().run()
}

/**
 * 重做
 */
const redo = () => {
  props.editor?.chain().focus().redo().run()
}
</script>
```

## 5. 后端实现方案

### 5.1 实体类设计

#### EditorContent.java - 编辑器内容实体
```java
package org.jeecg.modules.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 编辑器内容实体类
 * 用于存储Tiptap编辑器的内容数据，支持多种格式存储
 * 
 * @author AI Assistant
 * @since 2024-01-01
 */
@Data
@TableName("editor_content")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "EditorContent对象", description = "编辑器内容")
public class EditorContent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键ID")
    private String id;
    
    /**
     * 内容标题
     */
    @Excel(name = "内容标题", width = 15)
    @ApiModelProperty(value = "内容标题")
    private String title;
    
    /**
     * 编辑器内容(JSON格式)
     * 存储Tiptap编辑器的原始JSON数据
     */
    @ApiModelProperty(value = "编辑器内容(JSON格式)")
    private String content;
    
    /**
     * Markdown格式内容
     * 用于导出和兼容性处理
     */
    @ApiModelProperty(value = "Markdown格式内容")
    private String markdownContent;
    
    /**
     * HTML格式内容
     * 用于前端展示和SEO优化
     */
    @ApiModelProperty(value = "HTML格式内容")
    private String htmlContent;
    
    /**
     * 内容类型
     * 如：article(文章)、page(页面)、template(模板)等
     */
    @Excel(name = "内容类型", width = 15)
    @ApiModelProperty(value = "内容类型")
    private String contentType;
    
    /**
     * 状态
     * 0:草稿 1:发布 2:归档
     */
    @Excel(name = "状态", width = 15, dicCode = "content_status")
    @ApiModelProperty(value = "状态(0:草稿,1:发布,2:归档)")
    private Integer status;
    
    /**
     * 标签
     * 多个标签用逗号分隔
     */
    @Excel(name = "标签", width = 15)
    @ApiModelProperty(value = "标签")
    private String tags;
    
    /**
     * 分类ID
     */
    @Excel(name = "分类ID", width = 15)
    @ApiModelProperty(value = "分类ID")
    private String categoryId;
    
    /**
     * 作者ID
     */
    @Excel(name = "作者ID", width = 15)
    @ApiModelProperty(value = "作者ID")
    private String authorId;
    
    /**
     * 字数统计
     */
    @Excel(name = "字数统计", width = 15)
    @ApiModelProperty(value = "字数统计")
    private Integer wordCount;
    
    /**
     * 预计阅读时间(分钟)
     */
    @Excel(name = "预计阅读时间", width = 15)
    @ApiModelProperty(value = "预计阅读时间(分钟)")
    private Integer readingTime;
    
    /**
     * 是否为模板
     * 0:否 1:是
     */
    @Excel(name = "是否为模板", width = 15, dicCode = "yes_no")
    @ApiModelProperty(value = "是否为模板(0:否,1:是)")
    private Integer isTemplate;
    
    /**
     * 模板名称
     */
    @Excel(name = "模板名称", width = 15)
    @ApiModelProperty(value = "模板名称")
    private String templateName;
    
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    
    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    /**
     * 删除标志
     * 0:正常 1:删除
     */
    @TableLogic
    @ApiModelProperty(value = "删除标志(0:正常,1:删除)")
    private Integer delFlag;
}
```

### 5.2 VO类设计

#### EditorContentVO.java - 编辑器内容视图对象
```java
package org.jeecg.modules.content.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 编辑器内容视图对象
 * 用于前端展示的数据传输对象，包含格式化后的数据
 * 
 * @author AI Assistant
 * @since 2024-01-01
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "EditorContentVO对象", description = "编辑器内容视图对象")
public class EditorContentVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @ApiModelProperty(value = "主键ID")
    private String id;
    
    /**
     * 内容标题
     */
    @ApiModelProperty(value = "内容标题")
    private String title;
    
    /**
     * 编辑器内容(JSON格式)
     */
    @ApiModelProperty(value = "编辑器内容(JSON格式)")
    private String content;
    
    /**
     * Markdown格式内容
     */
    @ApiModelProperty(value = "Markdown格式内容")
    private String markdownContent;
    
    /**
     * HTML格式内容
     */
    @ApiModelProperty(value = "HTML格式内容")
    private String htmlContent;
    
    /**
     * 内容类型
     */
    @ApiModelProperty(value = "内容类型")
    private String contentType;
    
    /**
     * 内容类型名称
     */
    @ApiModelProperty(value = "内容类型名称")
    private String contentTypeName;
    
    /**
     * 状态
     */
    @ApiModelProperty(value = "状态(0:草稿,1:发布,2:归档)")
    private Integer status;
    
    /**
     * 状态名称
     */
    @ApiModelProperty(value = "状态名称")
    private String statusName;
    
    /**
     * 标签列表
     */
    @ApiModelProperty(value = "标签列表")
    private List<String> tagList;
    
    /**
     * 分类ID
     */
    @ApiModelProperty(value = "分类ID")
    private String categoryId;
    
    /**
     * 分类名称
     */
    @ApiModelProperty(value = "分类名称")
    private String categoryName;
    
    /**
     * 作者ID
     */
    @ApiModelProperty(value = "作者ID")
    private String authorId;
    
    /**
     * 作者姓名
     */
    @ApiModelProperty(value = "作者姓名")
    private String authorName;
    
    /**
     * 字数统计
     */
    @ApiModelProperty(value = "字数统计")
    private Integer wordCount;
    
    /**
     * 预计阅读时间(分钟)
     */
    @ApiModelProperty(value = "预计阅读时间(分钟)")
    private Integer readingTime;
    
    /**
     * 是否为模板
     */
    @ApiModelProperty(value = "是否为模板(0:否,1:是)")
    private Integer isTemplate;
    
    /**
     * 模板名称
     */
    @ApiModelProperty(value = "模板名称")
    private String templateName;
    
    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createBy;
    
    /**
     * 创建人姓名
     */
    @ApiModelProperty(value = "创建人姓名")
    private String createByName;
    
    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    
    /**
     * 更新人
     */
    @ApiModelProperty(value = "更新人")
    private String updateBy;
    
    /**
     * 更新人姓名
     */
    @ApiModelProperty(value = "更新人姓名")
    private String updateByName;
    
    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    
    /**
     * 内容摘要
     * 从HTML内容中提取的纯文本摘要
     */
    @ApiModelProperty(value = "内容摘要")
    private String summary;
    
    /**
     * 封面图片
     * 从内容中提取的第一张图片作为封面
     */
    @ApiModelProperty(value = "封面图片")
    private String coverImage;
}
```

### 5.3 控制器设计

#### EditorController.java - 编辑器控制器
```java
package org.jeecg.modules.content.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.content.entity.EditorContent;
import org.jeecg.modules.content.service.IEditorContentService;
import org.jeecg.modules.content.vo.EditorContentVO;
import org.jeecg.modules.content.vo.req.EditorContentCreateReqVO;
import org.jeecg.modules.content.vo.req.EditorContentUpdateReqVO;
import org.jeecg.modules.content.vo.req.EditorContentQueryReqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;

/**
 * Tiptap编辑器内容管理控制器
 * 提供编辑器内容的CRUD操作、格式转换、模板管理等功能
 * 
 * @author AI Assistant
 * @since 2024-01-01
 */
@Api(tags = "Tiptap编辑器内容管理")
@RestController
@RequestMapping("/content/editor")
@Slf4j
public class EditorController extends JeecgController<EditorContent, IEditorContentService> {
    
    @Autowired
    private IEditorContentService editorContentService;
    
    /**
     * 分页查询编辑器内容列表
     * 
     * @param queryReq 查询条件
     * @param pageNo 页码
     * @param pageSize 页大小
     * @param req HTTP请求对象
     * @return 分页结果
     */
    @AutoLog(value = "编辑器内容-分页列表查询")
    @ApiOperation(value = "编辑器内容-分页列表查询", notes = "编辑器内容-分页列表查询")
    @GetMapping(value = "/list")
    public Result<IPage<EditorContentVO>> queryPageList(
            EditorContentQueryReqVO queryReq,
            @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
            HttpServletRequest req) {
        
        // 构建查询条件
        QueryWrapper<EditorContent> queryWrapper = QueryGenerator.initQueryWrapper(new EditorContent(), req.getParameterMap());
        
        // 添加自定义查询条件
        if (queryReq.getTitle() != null) {
            queryWrapper.like("title", queryReq.getTitle());
        }
        if (queryReq.getContentType() != null) {
            queryWrapper.eq("content_type", queryReq.getContentType());
        }
        if (queryReq.getStatus() != null) {
            queryWrapper.eq("status", queryReq.getStatus());
        }
        if (queryReq.getAuthorId() != null) {
            queryWrapper.eq("author_id", queryReq.getAuthorId());
        }
        
        // 排序
        queryWrapper.orderByDesc("update_time");
        
        // 分页查询
        Page<EditorContent> page = new Page<>(pageNo, pageSize);
        IPage<EditorContent> pageList = editorContentService.page(page, queryWrapper);
        
        // 转换为VO对象
        IPage<EditorContentVO> result = editorContentService.convertToVOPage(pageList);
        
        return Result.OK(result);
    }
    
    /**
     * 根据ID查询编辑器内容详情
     * 
     * @param id 内容ID
     * @return 内容详情
     */
    @AutoLog(value = "编辑器内容-通过id查询")
    @ApiOperation(value = "编辑器内容-通过id查询", notes = "编辑器内容-通过id查询")
    @GetMapping(value = "/queryById")
    public Result<EditorContentVO> queryById(@ApiParam(name = "id", value = "ID", required = true) @RequestParam(name = "id", required = true) String id) {
        EditorContentVO result = editorContentService.getVOById(id);
        if (result == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(result);
    }
    
    /**
     * 创建编辑器内容
     * 
     * @param createReq 创建请求对象
     * @return 创建结果
     */
    @AutoLog(value = "编辑器内容-添加")
    @ApiOperation(value = "编辑器内容-添加", notes = "编辑器内容-添加")
    @PostMapping(value = "/add")
    public Result<String> add(@Valid @RequestBody EditorContentCreateReqVO createReq) {
        try {
            String id = editorContentService.createContent(createReq);
            return Result.OK("添加成功！", id);
        } catch (Exception e) {
            log.error("创建编辑器内容失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新编辑器内容
     * 
     * @param updateReq 更新请求对象
     * @return 更新结果
     */
    @AutoLog(value = "编辑器内容-编辑")
    @ApiOperation(value = "编辑器内容-编辑", notes = "编辑器内容-编辑")
    @PutMapping(value = "/edit")
    public Result<String> edit(@Valid @RequestBody EditorContentUpdateReqVO updateReq) {
        try {
            editorContentService.updateContent(updateReq);
            return Result.OK("编辑成功！");
        } catch (Exception e) {
            log.error("更新编辑器内容失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 批量删除编辑器内容
     * 
     * @param ids 内容ID列表，用逗号分隔
     * @return 删除结果
     */
    @AutoLog(value = "编辑器内容-通过id删除")
    @ApiOperation(value = "编辑器内容-通过id删除", notes = "编辑器内容-通过id删除")
    @DeleteMapping(value = "/delete")
    public Result<String> delete(@ApiParam(name = "ids", value = "ID列表", required = true) @RequestParam(name = "ids", required = true) String ids) {
        try {
            List<String> idList = Arrays.asList(ids.split(","));
            editorContentService.removeByIds(idList);
            return Result.OK("删除成功!");
        } catch (Exception e) {
            log.error("删除编辑器内容失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 发布内容
     * 
     * @param id 内容ID
     * @return 发布结果
     */
    @AutoLog(value = "编辑器内容-发布")
    @ApiOperation(value = "编辑器内容-发布", notes = "编辑器内容-发布")
    @PostMapping(value = "/publish/{id}")
    public Result<String> publish(@ApiParam(name = "id", value = "内容ID", required = true) @PathVariable String id) {
        try {
            editorContentService.publishContent(id);
            return Result.OK("发布成功！");
        } catch (Exception e) {
            log.error("发布内容失败", e);
            return Result.error("发布失败：" + e.getMessage());
        }
    }
    
    /**
     * 撤回发布
     * 
     * @param id 内容ID
     * @return 撤回结果
     */
    @AutoLog(value = "编辑器内容-撤回发布")
    @ApiOperation(value = "编辑器内容-撤回发布", notes = "编辑器内容-撤回发布")
    @PostMapping(value = "/unpublish/{id}")
    public Result<String> unpublish(@ApiParam(name = "id", value = "内容ID", required = true) @PathVariable String id) {
        try {
            editorContentService.unpublishContent(id);
            return Result.OK("撤回成功！");
        } catch (Exception e) {
            log.error("撤回发布失败", e);
            return Result.error("撤回失败：" + e.getMessage());
        }
    }
    
    /**
     * 内容格式转换
     * 支持JSON、Markdown、HTML之间的相互转换
     * 
     * @param id 内容ID
     * @param targetFormat 目标格式（json/markdown/html）
     * @return 转换后的内容
     */
    @AutoLog(value = "编辑器内容-格式转换")
    @ApiOperation(value = "编辑器内容-格式转换", notes = "编辑器内容-格式转换")
    @GetMapping(value = "/convert/{id}")
    public Result<String> convertFormat(
            @ApiParam(name = "id", value = "内容ID", required = true) @PathVariable String id,
            @ApiParam(name = "targetFormat", value = "目标格式", required = true) @RequestParam String targetFormat) {
        try {
            String convertedContent = editorContentService.convertFormat(id, targetFormat);
            return Result.OK(convertedContent);
        } catch (Exception e) {
            log.error("内容格式转换失败", e);
            return Result.error("转换失败：" + e.getMessage());
        }
    }
    
    /**
     * 保存为模板
     * 
     * @param id 内容ID
     * @param templateName 模板名称
     * @return 保存结果
     */
    @AutoLog(value = "编辑器内容-保存为模板")
    @ApiOperation(value = "编辑器内容-保存为模板", notes = "编辑器内容-保存为模板")
    @PostMapping(value = "/saveAsTemplate/{id}")
    public Result<String> saveAsTemplate(
            @ApiParam(name = "id", value = "内容ID", required = true) @PathVariable String id,
            @ApiParam(name = "templateName", value = "模板名称", required = true) @RequestParam String templateName) {
        try {
            editorContentService.saveAsTemplate(id, templateName);
            return Result.OK("保存模板成功！");
        } catch (Exception e) {
            log.error("保存模板失败", e);
            return Result.error("保存模板失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取模板列表
     * 
     * @return 模板列表
     */
    @AutoLog(value = "编辑器内容-获取模板列表")
    @ApiOperation(value = "编辑器内容-获取模板列表", notes = "编辑器内容-获取模板列表")
    @GetMapping(value = "/templates")
    public Result<List<EditorContentVO>> getTemplates() {
        try {
            List<EditorContentVO> templates = editorContentService.getTemplates();
            return Result.OK(templates);
        } catch (Exception e) {
            log.error("获取模板列表失败", e);
            return Result.error("获取模板列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 导出内容
     * 
     * @param id 内容ID
     * @param format 导出格式（markdown/html/pdf）
     * @param response HTTP响应对象
     */
    @AutoLog(value = "编辑器内容-导出")
    @ApiOperation(value = "编辑器内容-导出", notes = "编辑器内容-导出")
    @GetMapping(value = "/export/{id}")
    public void exportContent(
            @ApiParam(name = "id", value = "内容ID", required = true) @PathVariable String id,
            @ApiParam(name = "format", value = "导出格式", required = true) @RequestParam String format,
            HttpServletResponse response) {
        try {
            editorContentService.exportContent(id, format, response);
        } catch (Exception e) {
            log.error("导出内容失败", e);
        }
    }
}
```

## 6. 实现步骤指南

### 6.1 开发阶段划分

#### 第一阶段：基础架构搭建
1. **数据库设计**
   - 创建编辑器相关数据表
   - 设计索引和约束
   - 准备测试数据

2. **后端基础框架**
   - 创建实体类和VO类
   - 实现Mapper接口
   - 创建Service接口和实现类
   - 实现基础的CRUD操作

3. **前端基础组件**
   - 安装Tiptap相关依赖
   - 创建基础编辑器组件
   - 实现基本的编辑功能

#### 第二阶段：核心功能实现
1. **编辑器扩展**
   - 集成Markdown支持
   - 添加图片上传功能
   - 实现表格、链接等扩展
   - 添加代码高亮功能

2. **内容管理**
   - 实现内容的保存和加载
   - 添加版本历史功能
   - 实现内容格式转换
   - 添加模板功能

3. **用户界面优化**
   - 设计工具栏组件
   - 实现响应式布局
   - 添加快捷键支持
   - 优化用户体验

#### 第三阶段：高级功能开发
1. **协作功能**
   - 实现实时协作编辑
   - 添加评论和批注功能
   - 实现权限控制

2. **性能优化**
   - 实现内容懒加载
   - 添加缓存机制
   - 优化大文档处理

3. **扩展功能**
   - 添加导入导出功能
   - 实现主题切换
   - 集成第三方服务

### 6.2 技术实现要点

#### 前端实现要点
1. **组件化设计**
   - 将编辑器拆分为独立的组件
   - 使用组合式API管理状态
   - 实现组件间的通信机制

2. **状态管理**
   - 使用Pinia管理全局状态
   - 实现编辑器状态的持久化
   - 处理异步操作和错误状态

3. **性能优化**
   - 使用虚拟滚动处理大文档
   - 实现防抖和节流机制
   - 优化渲染性能

#### 后端实现要点
1. **数据处理**
   - 实现JSON、Markdown、HTML格式转换
   - 添加内容安全过滤
   - 实现文件上传和管理

2. **缓存策略**
   - 使用Redis缓存热点数据
   - 实现分布式缓存同步
   - 添加缓存失效机制

3. **安全控制**
   - 实现内容权限控制
   - 添加XSS防护
   - 实现操作日志记录

## 7. 测试方案

### 7.1 单元测试
- 编辑器组件功能测试
- 服务层业务逻辑测试
- 数据访问层测试
- 工具类方法测试

### 7.2 集成测试
- API接口测试
- 数据库操作测试
- 文件上传功能测试
- 格式转换功能测试

### 7.3 端到端测试
- 用户操作流程测试
- 浏览器兼容性测试
- 性能压力测试
- 安全性测试

## 8. 部署方案

### 8.1 开发环境
- 本地开发环境配置
- 数据库初始化脚本
- 依赖包安装指南

### 8.2 生产环境
- 服务器配置要求
- 数据库优化配置
- 缓存服务配置
- 负载均衡配置

## 9. 维护和扩展

### 9.1 日常维护
- 数据备份和恢复策略
- 性能监控和优化
- 安全漏洞修复
- 功能更新和升级

### 9.2 功能扩展
- 新扩展插件开发
- 第三方服务集成
- 移动端适配
- 国际化支持

## 10. 风险评估和应对

### 10.1 技术风险
- **依赖库版本兼容性**：定期更新依赖库，做好版本兼容性测试
- **浏览器兼容性**：支持主流浏览器，做好降级处理
- **性能瓶颈**：大文档处理性能优化，实现分页加载

### 10.2 业务风险
- **数据丢失**：实现自动保存和版本控制
- **安全漏洞**：定期安全审计，及时修复漏洞
- **用户体验**：持续收集用户反馈，优化交互设计

## 11. 成功标准

### 11.1 功能完整性
- ✅ 支持完整的Markdown语法
- ✅ 提供丰富的编辑器扩展
- ✅ 实现多格式内容转换
- ✅ 支持模板功能
- ✅ 提供协作编辑能力

### 11.2 性能指标
- 编辑器加载时间 < 2秒
- 大文档（10万字）编辑流畅度 > 60fps
- 文件上传成功率 > 99%
- 内容保存响应时间 < 1秒

### 11.3 用户体验
- 界面简洁美观，符合现代设计规范
- 操作直观易懂，学习成本低
- 支持快捷键操作，提高效率
- 响应式设计，适配多种设备

## 12. 开发时间估算

### 12.1 开发周期
- **第一阶段（基础架构）**：2-3周
- **第二阶段（核心功能）**：4-5周
- **第三阶段（高级功能）**：3-4周
- **测试和优化**：2-3周
- **总计**：11-15周

### 12.2 人力资源
- **前端开发工程师**：2人
- **后端开发工程师**：1人
- **UI/UX设计师**：1人
- **测试工程师**：1人
- **项目经理**：1人

## 13. 总结

本文档详细描述了基于Tiptap的富文本编辑器功能设计与实现方案。该方案具有以下特点：

1. **技术先进**：采用现代化的前端技术栈，确保系统的可维护性和扩展性
2. **功能完整**：支持完整的Markdown语法和丰富的扩展功能
3. **架构清晰**：采用分层架构设计，职责明确，便于开发和维护
4. **性能优秀**：通过多种优化手段，确保系统的高性能表现
5. **安全可靠**：实现完善的安全控制和数据保护机制

通过按照本文档的指导进行开发，可以构建出一个功能强大、性能优秀、用户体验良好的富文本编辑器系统，满足各种内容创作和管理需求。

---

**文档版本**：v1.0  
**创建日期**：2024-01-01  
**最后更新**：2024-01-01  
**作者**：AI Assistant