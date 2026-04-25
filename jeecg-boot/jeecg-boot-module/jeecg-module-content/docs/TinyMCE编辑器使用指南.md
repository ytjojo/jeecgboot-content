# TinyMCE 编辑器使用指南

## 1. 概述

TinyMCE 是一个功能强大的富文本编辑器，广泛应用于Web应用程序中。它提供了丰富的文本编辑功能，支持多种框架集成，并且具有高度的可定制性。

### 1.1 主要特性

- **跨平台兼容性**：支持所有主流浏览器
- **框架集成**：支持React、Angular、Vue.js、Blazor、Svelte等主流框架
- **丰富的插件系统**：提供大量开源和付费插件
- **高度可定制**：支持自定义主题、图标、工具栏等
- **移动端友好**：针对移动设备进行了优化
- **国际化支持**：支持多语言本地化

### 1.2 版本信息

- **当前最新版本**：TinyMCE 8
- **支持版本**：TinyMCE 5、6、7、8
- **升级路径**：提供完整的版本迁移指南

## 2. 安装方式

### 2.1 云端部署（推荐）

#### 2.1.1 快速开始

```html
<!DOCTYPE html>
<html>
<head>
    <script src="https://cdn.tiny.cloud/1/YOUR_API_KEY/tinymce/8/tinymce.min.js" referrerpolicy="origin"></script>
</head>
<body>
    <textarea id="mytextarea">Hello, World!</textarea>
    <script>
        tinymce.init({
            selector: '#mytextarea'
        });
    </script>
</body>
</html>
```

#### 2.1.2 获取API密钥

1. 访问 [Tiny Cloud](https://www.tiny.cloud/)
2. 注册账户并获取免费API密钥
3. 将API密钥替换到上述代码中的 `YOUR_API_KEY`

### 2.2 自托管部署

#### 2.2.1 NPM安装

```bash
npm install tinymce
```

#### 2.2.2 基本使用

```javascript
import tinymce from 'tinymce';

// 导入主题和插件
import 'tinymce/themes/silver';
import 'tinymce/plugins/advlist';
import 'tinymce/plugins/autolink';
import 'tinymce/plugins/lists';
import 'tinymce/plugins/link';
import 'tinymce/plugins/image';
import 'tinymce/plugins/charmap';
import 'tinymce/plugins/preview';
import 'tinymce/plugins/anchor';
import 'tinymce/plugins/searchreplace';
import 'tinymce/plugins/visualblocks';
import 'tinymce/plugins/code';
import 'tinymce/plugins/fullscreen';
import 'tinymce/plugins/insertdatetime';
import 'tinymce/plugins/media';
import 'tinymce/plugins/table';
import 'tinymce/plugins/help';
import 'tinymce/plugins/wordcount';

// 初始化编辑器
tinymce.init({
    selector: '#mytextarea',
    plugins: 'advlist autolink lists link image charmap preview anchor searchreplace visualblocks code fullscreen insertdatetime media table help wordcount',
    toolbar: 'undo redo | blocks | bold italic forecolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | removeformat | help'
});
```

## 3. 框架集成

### 3.1 React集成

#### 3.1.1 安装依赖

```bash
npm install @tinymce/tinymce-react
```

#### 3.1.2 基本使用

```jsx
import React, { useRef } from 'react';
import { Editor } from '@tinymce/tinymce-react';

export default function App() {
    const editorRef = useRef(null);
    
    const log = () => {
        if (editorRef.current) {
            console.log(editorRef.current.getContent());
        }
    };
    
    return (
        <>
            <Editor
                apiKey='YOUR_API_KEY'
                onInit={(evt, editor) => editorRef.current = editor}
                initialValue="<p>这是初始内容!</p>"
                init={{
                    height: 500,
                    menubar: false,
                    plugins: [
                        'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
                        'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
                        'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount'
                    ],
                    toolbar: 'undo redo | blocks | ' +
                        'bold italic forecolor | alignleft aligncenter ' +
                        'alignright alignjustify | bullist numlist outdent indent | ' +
                        'removeformat | help',
                    content_style: 'body { font-family:Helvetica,Arial,sans-serif; font-size:14px }'
                }}
            />
            <button onClick={log}>获取内容</button>
        </>
    );
}
```

### 3.2 Vue.js集成

#### 3.2.1 安装依赖

```bash
npm install @tinymce/tinymce-vue
```

#### 3.2.2 基本使用

```vue
<template>
    <div>
        <Editor
            api-key="YOUR_API_KEY"
            :init="init"
            v-model="content"
        />
        <button @click="log">获取内容</button>
    </div>
</template>

<script>
import Editor from '@tinymce/tinymce-vue'

export default {
    components: {
        Editor
    },
    data() {
        return {
            content: '<p>这是初始内容!</p>',
            init: {
                height: 500,
                menubar: false,
                plugins: [
                    'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
                    'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
                    'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount'
                ],
                toolbar: 'undo redo | blocks | ' +
                    'bold italic forecolor | alignleft aligncenter ' +
                    'alignright alignjustify | bullist numlist outdent indent | ' +
                    'removeformat | help',
                content_style: 'body { font-family:Helvetica,Arial,sans-serif; font-size:14px }'
            }
        }
    },
    methods: {
        log() {
            console.log(this.content);
        }
    }
}
</script>
```

### 3.3 Angular集成

#### 3.3.1 安装依赖

```bash
npm install @tinymce/tinymce-angular
```

#### 3.3.2 模块导入

```typescript
// app.module.ts
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { EditorModule } from '@tinymce/tinymce-angular';

import { AppComponent } from './app.component';

@NgModule({
    declarations: [
        AppComponent
    ],
    imports: [
        BrowserModule,
        EditorModule
    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule { }
```

#### 3.3.3 组件使用

```typescript
// app.component.ts
import { Component } from '@angular/core';

@Component({
    selector: 'app-root',
    template: `
        <editor
            apiKey="YOUR_API_KEY"
            [init]="init"
            [(ngModel)]="content">
        </editor>
        <button (click)="log()">获取内容</button>
    `
})
export class AppComponent {
    content = '<p>这是初始内容!</p>';
    
    init = {
        height: 500,
        menubar: false,
        plugins: [
            'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
            'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
            'insertdatetime', 'media', 'table', 'code', 'help', 'wordcount'
        ],
        toolbar: 'undo redo | blocks | ' +
            'bold italic forecolor | alignleft aligncenter ' +
            'alignright alignjustify | bullist numlist outdent indent | ' +
            'removeformat | help',
        content_style: 'body { font-family:Helvetica,Arial,sans-serif; font-size:14px }'
    };
    
    log() {
        console.log(this.content);
    }
}
```

## 4. 编辑器模式

### 4.1 经典模式（Classic Mode）

经典模式是最常用的编辑器模式，编辑器替换textarea元素。

```javascript
tinymce.init({
    selector: '#mytextarea',
    // 其他配置...
});
```

### 4.2 内联模式（Inline Mode）

内联模式允许直接在页面内容上进行编辑。

```javascript
tinymce.init({
    selector: '#myeditablediv',
    inline: true,
    // 其他配置...
});
```

```html
<div id="myeditablediv" contenteditable="true">
    <p>这里的内容可以直接编辑</p>
</div>
```

### 4.3 无干扰模式（Distraction-free Mode）

无干扰模式提供了一个简洁的编辑环境。

```javascript
tinymce.init({
    selector: '#mytextarea',
    menubar: false,
    toolbar: false,
    statusbar: false,
    // 其他配置...
});
```

## 5. 核心配置选项

### 5.1 基础配置

```javascript
tinymce.init({
    // 选择器
    selector: '#mytextarea',
    
    // 编辑器高度和宽度
    height: 500,
    width: 800,
    
    // 语言设置
    language: 'zh_CN',
    
    // 主题
    theme: 'silver',
    
    // 皮肤
    skin: 'oxide',
    
    // 图标包
    icons: 'default',
    
    // 初始内容
    content: '<p>初始内容</p>',
    
    // 占位符
    placeholder: '请输入内容...',
    
    // 是否可调整大小
    resize: true,
    
    // 自动聚焦
    auto_focus: false
});
```

### 5.2 工具栏配置

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 工具栏配置
    toolbar: [
        'undo redo | blocks fontfamily fontsize',
        'bold italic underline strikethrough | link image media table mergetags',
        'align lineheight | tinycomments | checklist numlist bullist indent outdent',
        'emoticons charmap | removeformat'
    ],
    
    // 或者使用字符串格式
    toolbar: 'undo redo | blocks | bold italic | alignleft aligncenter alignright | bullist numlist',
    
    // 工具栏模式
    toolbar_mode: 'sliding', // 'floating', 'sliding', 'scrolling', 'wrap'
    
    // 隐藏菜单栏
    menubar: false,
    
    // 或者自定义菜单栏
    menubar: 'file edit view insert format tools table help'
});
```

### 5.3 插件配置

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 插件列表
    plugins: [
        // 基础插件
        'advlist', 'autolink', 'lists', 'link', 'image', 'charmap', 'preview',
        'anchor', 'searchreplace', 'visualblocks', 'code', 'fullscreen',
        'insertdatetime', 'media', 'table', 'help', 'wordcount',
        
        // 高级插件（需要付费）
        'checklist', 'mediaembed', 'casechange', 'export', 'formatpainter',
        'pageembed', 'a11ychecker', 'tinymcespellchecker', 'permanentpen',
        'powerpaste', 'advtable', 'advcode', 'editimage', 'advtemplate',
        'ai', 'mentions', 'tinycomments', 'tableofcontents', 'footnotes',
        'mergetags', 'autocorrect', 'typography', 'inlinecss', 'markdown'
    ]
});
```

## 6. 常用插件详解

### 6.1 开源插件

#### 6.1.1 Lists（列表）

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'lists',
    toolbar: 'bullist numlist'
});
```

#### 6.1.2 Link（链接）

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'link',
    toolbar: 'link unlink',
    
    // 链接配置
    link_assume_external_targets: true,
    link_context_toolbar: true,
    link_default_target: '_blank'
});
```

#### 6.1.3 Image（图片）

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'image',
    toolbar: 'image',
    
    // 图片上传配置
    images_upload_url: '/upload',
    images_upload_handler: function (blobInfo, success, failure) {
        // 自定义上传处理
        const formData = new FormData();
        formData.append('file', blobInfo.blob(), blobInfo.filename());
        
        fetch('/upload', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(result => {
            success(result.location);
        })
        .catch(error => {
            failure('上传失败: ' + error.message);
        });
    }
});
```

#### 6.1.4 Table（表格）

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'table',
    toolbar: 'table',
    
    // 表格配置
    table_default_attributes: {
        border: '1'
    },
    table_default_styles: {
        'border-collapse': 'collapse',
        'width': '100%'
    }
});
```

### 6.2 付费插件

#### 6.2.1 AI Assistant（AI助手）

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'ai',
    toolbar: 'aidialog aishortcuts',
    
    // AI配置
    ai_request: (request, respondWith) => {
        // 集成OpenAI、Azure AI等
        const openAiOptions = {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${API_KEY}`
            },
            body: JSON.stringify({
                model: 'gpt-3.5-turbo',
                messages: [{ role: 'user', content: request.prompt }]
            })
        };
        
        respondWith.string(() => fetch('https://api.openai.com/v1/chat/completions', openAiOptions)
            .then(response => response.json())
            .then(data => data.choices[0].message.content)
        );
    }
});
```

#### 6.2.2 Comments（评论）

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'tinycomments',
    toolbar: 'addcomment showcomments deleteallconversations',
    
    // 评论配置
    tinycomments_mode: 'embedded', // 或 'callback'
    tinycomments_author: 'Author Name',
    tinycomments_author_avatar: 'https://example.com/avatar.jpg'
});
```

#### 6.2.3 Advanced Typography（高级排版）

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'typography',
    toolbar: 'typography',
    
    // 排版规则
    typography_rules: [
        'common/punctuation/quotes',
        'en-US/dash/spacing',
        'common/punctuation/apostrophe'
    ]
});
```

## 7. 内容处理

### 7.1 内容过滤

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 有效元素
    valid_elements: 'p,br,strong,em,ul,ol,li,a[href],img[src|alt]',
    
    // 扩展有效元素
    extended_valid_elements: 'script[src|async|defer|type|charset]',
    
    // 无效元素
    invalid_elements: 'font,center',
    
    // 内容过滤
    paste_data_images: true,
    paste_as_text: false,
    paste_preprocess: function(plugin, args) {
        // 预处理粘贴内容
        args.content = args.content.replace(/font-size:\s*\d+px/gi, '');
    }
});
```

### 7.2 内容样式

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 内容CSS
    content_css: [
        '//fonts.googleapis.com/css?family=Lato:300,300i,400,400i',
        '//www.tiny.cloud/css/codepen.min.css'
    ],
    
    // 内容样式
    content_style: `
        body { 
            font-family: Helvetica, Arial, sans-serif; 
            font-size: 14px;
            line-height: 1.6;
            color: #333;
        }
        p { margin: 0 0 10px 0; }
        h1, h2, h3, h4, h5, h6 { margin: 0 0 15px 0; }
    `,
    
    // 格式选项
    formats: {
        alignleft: { selector: 'p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li,table,img', classes: 'left' },
        aligncenter: { selector: 'p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li,table,img', classes: 'center' },
        alignright: { selector: 'p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li,table,img', classes: 'right' },
        alignjustify: { selector: 'p,h1,h2,h3,h4,h5,h6,td,th,div,ul,ol,li,table,img', classes: 'full' },
        bold: { inline: 'span', 'classes': 'bold' },
        italic: { inline: 'span', 'classes': 'italic' },
        underline: { inline: 'span', 'classes': 'underline', exact: true },
        strikethrough: { inline: 'del' }
    }
});
```

### 7.3 文件上传

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'image media link',
    
    // 图片上传
    images_upload_url: '/api/upload/image',
    images_upload_base_path: '/uploads/',
    images_upload_credentials: true,
    images_upload_handler: function (blobInfo, success, failure, progress) {
        const xhr = new XMLHttpRequest();
        xhr.withCredentials = false;
        xhr.open('POST', '/api/upload/image');
        
        xhr.upload.onprogress = function (e) {
            progress(e.loaded / e.total * 100);
        };
        
        xhr.onload = function() {
            if (xhr.status === 403) {
                failure('HTTP Error: ' + xhr.status, { remove: true });
                return;
            }
            
            if (xhr.status < 200 || xhr.status >= 300) {
                failure('HTTP Error: ' + xhr.status);
                return;
            }
            
            const json = JSON.parse(xhr.responseText);
            
            if (!json || typeof json.location != 'string') {
                failure('Invalid JSON: ' + xhr.responseText);
                return;
            }
            
            success(json.location);
        };
        
        xhr.onerror = function () {
            failure('Image upload failed due to a XHR Transport error. Code: ' + xhr.status);
        };
        
        const formData = new FormData();
        formData.append('file', blobInfo.blob(), blobInfo.filename());
        
        xhr.send(formData);
    },
    
    // 文件选择器回调
    file_picker_callback: function (callback, value, meta) {
        // 创建文件输入元素
        const input = document.createElement('input');
        input.setAttribute('type', 'file');
        input.setAttribute('accept', meta.filetype === 'image' ? 'image/*' : '*/*');
        
        input.onchange = function () {
            const file = this.files[0];
            const reader = new FileReader();
            
            reader.onload = function () {
                const id = 'blobid' + (new Date()).getTime();
                const blobCache = tinymce.activeEditor.editorUpload.blobCache;
                const base64 = reader.result.split(',')[1];
                const blobInfo = blobCache.create(id, file, base64);
                blobCache.add(blobInfo);
                
                callback(blobInfo.blobUri(), { title: file.name });
            };
            
            reader.readAsDataURL(file);
        };
        
        input.click();
    }
});
```

## 8. 事件处理

### 8.1 编辑器事件

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 初始化完成
    init_instance_callback: function (editor) {
        console.log('编辑器已初始化: ' + editor.id);
    },
    
    // 设置事件监听器
    setup: function (editor) {
        // 内容变化事件
        editor.on('change', function (e) {
            console.log('内容已更改');
        });
        
        // 键盘事件
        editor.on('keydown', function (e) {
            if (e.keyCode === 13) { // Enter键
                console.log('按下了Enter键');
            }
        });
        
        // 焦点事件
        editor.on('focus', function (e) {
            console.log('编辑器获得焦点');
        });
        
        editor.on('blur', function (e) {
            console.log('编辑器失去焦点');
        });
        
        // 选择变化事件
        editor.on('selectionchange', function (e) {
            console.log('选择已更改');
        });
        
        // 节点变化事件
        editor.on('nodechange', function (e) {
            console.log('节点已更改: ' + e.element.tagName);
        });
        
        // 保存前事件
        editor.on('beforesave', function (e) {
            console.log('即将保存内容');
        });
        
        // 自定义工具栏按钮
        editor.ui.registry.addButton('customButton', {
            text: '自定义按钮',
            onAction: function () {
                editor.insertContent('<p>插入的自定义内容</p>');
            }
        });
    }
});
```

### 8.2 API调用

```javascript
// 获取编辑器实例
const editor = tinymce.get('mytextarea');

// 或者通过activeEditor
const editor = tinymce.activeEditor;

// 获取内容
const content = editor.getContent();
const textContent = editor.getContent({ format: 'text' });

// 设置内容
editor.setContent('<p>新的内容</p>');

// 插入内容
editor.insertContent('<p>插入的内容</p>');

// 执行命令
editor.execCommand('Bold');
editor.execCommand('FontSize', false, '14px');

// 聚焦编辑器
editor.focus();

// 保存内容
editor.save();

// 销毁编辑器
editor.destroy();

// 获取选中的内容
const selectedContent = editor.selection.getContent();

// 设置选中的内容
editor.selection.setContent('<strong>加粗的文本</strong>');

// 获取选中的节点
const selectedNode = editor.selection.getNode();

// 移动光标到内容末尾
editor.selection.select(editor.getBody(), true);
editor.selection.collapse(false);
```

## 9. 自定义开发

### 9.1 自定义工具栏按钮

```javascript
tinymce.init({
    selector: '#mytextarea',
    toolbar: 'customButton customToggleButton customMenuButton',
    
    setup: function (editor) {
        // 基础按钮
        editor.ui.registry.addButton('customButton', {
            text: '插入时间',
            tooltip: '插入当前时间',
            onAction: function () {
                const now = new Date();
                editor.insertContent('<p>当前时间: ' + now.toLocaleString() + '</p>');
            }
        });
        
        // 切换按钮
        editor.ui.registry.addToggleButton('customToggleButton', {
            text: '高亮',
            tooltip: '切换高亮显示',
            onAction: function () {
                editor.execCommand('HiliteColor', false, '#ffff00');
            },
            onSetup: function (api) {
                const changed = editor.formatter.formatChanged('hilitecolor', function (state) {
                    api.setActive(state);
                });
                return function () {
                    changed.unbind();
                };
            }
        });
        
        // 菜单按钮
        editor.ui.registry.addMenuButton('customMenuButton', {
            text: '插入模板',
            fetch: function (callback) {
                const items = [
                    {
                        type: 'menuitem',
                        text: '标题模板',
                        onAction: function () {
                            editor.insertContent('<h1>标题</h1><p>内容...</p>');
                        }
                    },
                    {
                        type: 'menuitem',
                        text: '列表模板',
                        onAction: function () {
                            editor.insertContent('<ul><li>项目1</li><li>项目2</li><li>项目3</li></ul>');
                        }
                    }
                ];
                callback(items);
            }
        });
    }
});
```

### 9.2 自定义插件

```javascript
// 创建自定义插件
(function () {
    'use strict';
    
    tinymce.PluginManager.add('customPlugin', function (editor, url) {
        // 插件初始化
        editor.on('init', function () {
            console.log('自定义插件已加载');
        });
        
        // 添加工具栏按钮
        editor.ui.registry.addButton('customPluginButton', {
            text: '自定义功能',
            tooltip: '执行自定义功能',
            onAction: function () {
                // 打开对话框
                editor.windowManager.open({
                    title: '自定义对话框',
                    body: {
                        type: 'panel',
                        items: [
                            {
                                type: 'input',
                                name: 'title',
                                label: '标题'
                            },
                            {
                                type: 'textarea',
                                name: 'content',
                                label: '内容'
                            }
                        ]
                    },
                    buttons: [
                        {
                            type: 'cancel',
                            text: '取消'
                        },
                        {
                            type: 'submit',
                            text: '确定',
                            primary: true
                        }
                    ],
                    onSubmit: function (api) {
                        const data = api.getData();
                        const html = `<h3>${data.title}</h3><p>${data.content}</p>`;
                        editor.insertContent(html);
                        api.close();
                    }
                });
            }
        });
        
        // 添加菜单项
        editor.ui.registry.addMenuItem('customPluginMenuItem', {
            text: '自定义菜单项',
            onAction: function () {
                editor.insertContent('<p>通过菜单插入的内容</p>');
            }
        });
        
        // 返回插件信息
        return {
            getMetadata: function () {
                return {
                    name: '自定义插件',
                    url: 'https://example.com'
                };
            }
        };
    });
})();

// 使用自定义插件
tinymce.init({
    selector: '#mytextarea',
    plugins: 'customPlugin',
    toolbar: 'customPluginButton',
    menubar: 'custom',
    menu: {
        custom: { title: '自定义', items: 'customPluginMenuItem' }
    }
});
```

### 9.3 自定义对话框

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    setup: function (editor) {
        editor.ui.registry.addButton('openDialog', {
            text: '打开对话框',
            onAction: function () {
                editor.windowManager.open({
                    title: '高级设置',
                    body: {
                        type: 'panel',
                        items: [
                            {
                                type: 'input',
                                name: 'title',
                                label: '标题',
                                placeholder: '请输入标题'
                            },
                            {
                                type: 'selectbox',
                                name: 'level',
                                label: '级别',
                                items: [
                                    { text: 'H1', value: 'h1' },
                                    { text: 'H2', value: 'h2' },
                                    { text: 'H3', value: 'h3' }
                                ]
                            },
                            {
                                type: 'checkbox',
                                name: 'bold',
                                label: '加粗'
                            },
                            {
                                type: 'colorinput',
                                name: 'color',
                                label: '颜色'
                            },
                            {
                                type: 'textarea',
                                name: 'content',
                                label: '内容',
                                placeholder: '请输入内容'
                            }
                        ]
                    },
                    buttons: [
                        {
                            type: 'cancel',
                            text: '取消'
                        },
                        {
                            type: 'submit',
                            text: '插入',
                            primary: true
                        }
                    ],
                    initialData: {
                        title: '',
                        level: 'h2',
                        bold: false,
                        color: '#000000',
                        content: ''
                    },
                    onSubmit: function (api) {
                        const data = api.getData();
                        let html = `<${data.level} style="color: ${data.color};">`;
                        
                        if (data.bold) {
                            html += `<strong>${data.title}</strong>`;
                        } else {
                            html += data.title;
                        }
                        
                        html += `</${data.level}>`;
                        
                        if (data.content) {
                            html += `<p>${data.content}</p>`;
                        }
                        
                        editor.insertContent(html);
                        api.close();
                    }
                });
            }
        });
    }
});
```

## 10. 国际化配置

### 10.1 语言包设置

```javascript
// 云端部署 - 自动加载语言包
tinymce.init({
    selector: '#mytextarea',
    language: 'zh_CN' // 中文简体
});

// 自托管 - 需要手动加载语言包
tinymce.init({
    selector: '#mytextarea',
    language: 'zh_CN',
    language_url: '/path/to/langs/zh_CN.js'
});
```

### 10.2 支持的语言

- `zh_CN` - 中文简体
- `zh_TW` - 中文繁体
- `en` - 英语
- `ja` - 日语
- `ko` - 韩语
- `fr_FR` - 法语
- `de` - 德语
- `es` - 西班牙语
- `it` - 意大利语
- `pt_BR` - 葡萄牙语（巴西）
- `ru` - 俄语
- `ar` - 阿拉伯语

### 10.3 自定义翻译

```javascript
tinymce.addI18n('zh_CN', {
    'Bold': '加粗',
    'Italic': '斜体',
    'Underline': '下划线',
    'Insert/edit link': '插入/编辑链接',
    'Insert/edit image': '插入/编辑图片',
    'Custom button': '自定义按钮'
});
```

## 11. 性能优化

### 11.1 延迟加载

```javascript
// 延迟初始化编辑器
function initEditor() {
    if (!window.tinymceLoaded) {
        const script = document.createElement('script');
        script.src = 'https://cdn.tiny.cloud/1/YOUR_API_KEY/tinymce/8/tinymce.min.js';
        script.onload = function() {
            window.tinymceLoaded = true;
            tinymce.init({
                selector: '#mytextarea'
            });
        };
        document.head.appendChild(script);
    } else {
        tinymce.init({
            selector: '#mytextarea'
        });
    }
}

// 在需要时调用
document.getElementById('loadEditor').addEventListener('click', initEditor);
```

### 11.2 按需加载插件

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 只加载必需的插件
    plugins: 'lists link image table code',
    
    // 延迟加载高级功能
    setup: function(editor) {
        editor.on('focus', function() {
            // 在编辑器获得焦点时加载额外插件
            if (!editor.plugins.advlist) {
                tinymce.PluginManager.load('advlist', '/path/to/advlist/plugin.min.js');
            }
        });
    }
});
```

### 11.3 内容优化

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 禁用不必要的功能
    branding: false,
    elementpath: false,
    resize: false,
    
    // 优化内容处理
    paste_data_images: false, // 禁用粘贴图片
    paste_as_text: true, // 粘贴为纯文本
    
    // 限制撤销级别
    custom_undo_redo_levels: 10,
    
    // 优化自动保存
    autosave_ask_before_unload: false,
    autosave_interval: '30s'
});
```

## 12. 安全配置

### 12.1 内容安全策略

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 有效元素白名单
    valid_elements: 'p,br,strong,em,h1,h2,h3,h4,h5,h6,ul,ol,li,a[href|target],img[src|alt|width|height]',
    
    // 禁用危险元素
    invalid_elements: 'script,object,embed,iframe,form,input,button',
    
    // 清理粘贴内容
    paste_preprocess: function(plugin, args) {
        // 移除所有脚本标签
        args.content = args.content.replace(/<script[^>]*>.*?<\/script>/gi, '');
        // 移除事件处理器
        args.content = args.content.replace(/on\w+="[^"]*"/gi, '');
        // 移除javascript:链接
        args.content = args.content.replace(/href="javascript:[^"]*"/gi, 'href="#"');
    },
    
    // URL验证
    urlconverter_callback: function(url, node, on_save, name) {
        // 只允许HTTP和HTTPS链接
        if (url.startsWith('javascript:') || url.startsWith('data:')) {
            return '';
        }
        return url;
    }
});
```

### 12.2 文件上传安全

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'image',
    
    // 限制文件类型
    file_picker_types: 'image',
    
    // 图片上传验证
    images_upload_handler: function (blobInfo, success, failure) {
        // 验证文件类型
        const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
        if (!allowedTypes.includes(blobInfo.blob().type)) {
            failure('不支持的文件类型');
            return;
        }
        
        // 验证文件大小（5MB限制）
        if (blobInfo.blob().size > 5 * 1024 * 1024) {
            failure('文件大小不能超过5MB');
            return;
        }
        
        // 验证文件名
        const filename = blobInfo.filename();
        if (!/^[a-zA-Z0-9._-]+$/.test(filename)) {
            failure('文件名包含非法字符');
            return;
        }
        
        // 执行上传
        const formData = new FormData();
        formData.append('file', blobInfo.blob(), filename);
        
        fetch('/api/upload', {
            method: 'POST',
            body: formData,
            headers: {
                'X-CSRF-Token': document.querySelector('meta[name="csrf-token"]').content
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('上传失败');
            }
            return response.json();
        })
        .then(result => {
            success(result.url);
        })
        .catch(error => {
            failure(error.message);
        });
    }
});
```

## 13. 移动端适配

### 13.1 响应式配置

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 移动端主题
    mobile: {
        theme: 'mobile',
        plugins: 'lists link image',
        toolbar: 'undo redo | bold italic | bullist numlist | link image'
    },
    
    // 桌面端配置
    theme: 'silver',
    plugins: 'advlist autolink lists link image charmap preview anchor searchreplace visualblocks code fullscreen insertdatetime media table help wordcount',
    toolbar: 'undo redo | blocks | bold italic forecolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | removeformat | help',
    
    // 自适应高度
    height: window.innerWidth < 768 ? 300 : 500,
    
    // 触摸优化
    touch_ui: true
});
```

### 13.2 移动端工具栏

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 移动端简化工具栏
    mobile: {
        toolbar_mode: 'scrolling',
        toolbar: [
            'undo redo',
            'bold italic underline',
            'alignleft aligncenter alignright',
            'bullist numlist',
            'link image'
        ]
    }
});
```

## 14. 常见问题解决

### 14.1 编辑器无法加载

```javascript
// 检查API密钥
tinymce.init({
    selector: '#mytextarea',
    
    // 错误处理
    init_instance_callback: function (editor) {
        console.log('编辑器加载成功: ' + editor.id);
    },
    
    // 加载失败处理
    setup: function (editor) {
        editor.on('LoadError', function (e) {
            console.error('编辑器加载失败:', e);
            // 显示错误信息给用户
            document.getElementById('editor-error').style.display = 'block';
        });
    }
});

// 超时检测
setTimeout(function() {
    if (!tinymce.get('mytextarea')) {
        console.error('编辑器加载超时');
        // 尝试重新加载或显示备用编辑器
    }
}, 10000);
```

### 14.2 内容保存问题

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 自动保存
    autosave_ask_before_unload: true,
    autosave_interval: '30s',
    autosave_prefix: 'tinymce-autosave-{path}{query}-{id}-',
    autosave_restore_when_empty: false,
    autosave_retention: '2m',
    
    // 保存前验证
    save_onsavecallback: function () {
        const content = tinymce.activeEditor.getContent();
        if (content.length < 10) {
            alert('内容太短，请输入更多内容');
            return false;
        }
        return true;
    }
});

// 手动保存
function saveContent() {
    const editor = tinymce.get('mytextarea');
    if (editor) {
        const content = editor.getContent();
        
        // 发送到服务器
        fetch('/api/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ content: content })
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                console.log('保存成功');
            } else {
                console.error('保存失败:', data.error);
            }
        })
        .catch(error => {
            console.error('保存出错:', error);
        });
    }
}
```

### 14.3 样式冲突问题

```javascript
tinymce.init({
    selector: '#mytextarea',
    
    // 隔离编辑器样式
    content_css: false,
    content_style: `
        /* 重置样式 */
        * { box-sizing: border-box; }
        body { 
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            font-size: 14px;
            line-height: 1.6;
            color: #333;
            margin: 8px;
        }
        /* 防止样式冲突 */
        .mce-content-body * {
            margin: 0;
            padding: 0;
        }
        .mce-content-body p {
            margin: 0 0 10px 0;
        }
    `,
    
    // 使用iframe模式避免样式冲突
    inline: false
});
```

## 15. 最佳实践

### 15.1 性能最佳实践

1. **按需加载插件**：只加载必要的插件
2. **使用CDN**：利用Tiny Cloud CDN提高加载速度
3. **延迟初始化**：在用户需要时才初始化编辑器
4. **内容优化**：限制撤销级别，优化自动保存频率
5. **图片优化**：压缩上传图片，使用适当的格式

### 15.2 安全最佳实践

1. **内容过滤**：使用白名单过滤允许的HTML元素
2. **文件上传验证**：验证文件类型、大小和名称
3. **XSS防护**：清理用户输入，防止脚本注入
4. **CSRF保护**：在上传请求中包含CSRF令牌
5. **URL验证**：验证链接URL的安全性

### 15.3 用户体验最佳实践

1. **响应式设计**：适配不同屏幕尺寸
2. **加载状态**：显示编辑器加载进度
3. **错误处理**：友好的错误提示信息
4. **自动保存**：防止用户数据丢失
5. **键盘快捷键**：提供常用操作的快捷键

### 15.4 维护最佳实践

1. **版本管理**：定期更新到最新稳定版本
2. **配置管理**：集中管理编辑器配置
3. **监控日志**：记录编辑器错误和性能指标
4. **测试覆盖**：编写自动化测试用例
5. **文档维护**：保持配置文档的更新

## 16. 总结

TinyMCE是一个功能强大且高度可定制的富文本编辑器，适用于各种Web应用场景。通过合理的配置和优化，可以为用户提供优秀的编辑体验。

### 16.1 选择建议

- **小型项目**：使用云端部署，配置简单的工具栏和插件
- **中型项目**：自托管部署，根据需求选择插件
- **大型项目**：考虑付费插件，实现高级功能
- **企业级应用**：使用完整的安全配置和性能优化

### 16.2 学习路径

1. **基础使用**：掌握基本配置和常用插件
2. **框架集成**：学习在React、Vue等框架中的使用
3. **高级定制**：开发自定义插件和组件
4. **性能优化**：实现最佳的用户体验
5. **安全加固**：确保应用的安全性

## 17. Markdown 支持

### 17.1 Markdown 支持现状

TinyMCE 本身是一个所见即所得（WYSIWYG）的富文本编辑器，**不原生支持 Markdown 格式**。它主要处理 HTML 格式的内容。但是，可以通过以下方式实现 Markdown 支持：

### 17.2 实现 Markdown 支持的方案

#### 17.2.1 方案一：转换库集成

使用第三方转换库在保存和加载时进行格式转换：

```javascript
// 安装依赖
// npm install marked turndown

import { marked } from 'marked';
import TurndownService from 'turndown';

const turndownService = new TurndownService();

// Markdown 转 HTML（加载时）
function markdownToHtml(markdown) {
    return marked(markdown);
}

// HTML 转 Markdown（保存时）
function htmlToMarkdown(html) {
    return turndownService.turndown(html);
}

// TinyMCE 配置
tinymce.init({
    selector: '#mytextarea',
    setup: function(editor) {
        // 加载时转换 Markdown 为 HTML
        editor.on('init', function() {
            const markdownContent = getMarkdownFromServer(); // 从服务器获取 Markdown
            const htmlContent = markdownToHtml(markdownContent);
            editor.setContent(htmlContent);
        });
        
        // 保存时转换 HTML 为 Markdown
        editor.on('beforesave', function() {
            const htmlContent = editor.getContent();
            const markdownContent = htmlToMarkdown(htmlContent);
            saveMarkdownToServer(markdownContent); // 保存 Markdown 到服务器
        });
    }
});
```

#### 17.2.2 方案二：双模式编辑器

实现富文本和 Markdown 模式之间的切换：

```javascript
let isMarkdownMode = false;
let markdownTextarea = null;

tinymce.init({
    selector: '#mytextarea',
    setup: function(editor) {
        // 添加 Markdown 切换按钮
        editor.ui.registry.addButton('markdown', {
            text: 'Markdown',
            tooltip: '切换到 Markdown 模式',
            onAction: function() {
                toggleMarkdownMode(editor);
            }
        });
        
        // 添加预览按钮
        editor.ui.registry.addButton('preview', {
            text: '预览',
            tooltip: '预览 Markdown',
            onAction: function() {
                previewMarkdown(editor);
            }
        });
    },
    toolbar: 'markdown preview | bold italic | bullist numlist'
});

function toggleMarkdownMode(editor) {
    if (!isMarkdownMode) {
        // 切换到 Markdown 模式
        const htmlContent = editor.getContent();
        const markdownContent = htmlToMarkdown(htmlContent);
        
        // 隐藏 TinyMCE 编辑器
        editor.hide();
        
        // 创建 Markdown 文本域
        markdownTextarea = document.createElement('textarea');
        markdownTextarea.value = markdownContent;
        markdownTextarea.style.width = '100%';
        markdownTextarea.style.height = '400px';
        markdownTextarea.style.fontFamily = 'monospace';
        
        editor.getContainer().appendChild(markdownTextarea);
        isMarkdownMode = true;
    } else {
        // 切换回富文本模式
        const markdownContent = markdownTextarea.value;
        const htmlContent = markdownToHtml(markdownContent);
        
        // 移除 Markdown 文本域
        markdownTextarea.remove();
        markdownTextarea = null;
        
        // 显示 TinyMCE 编辑器并设置内容
        editor.show();
        editor.setContent(htmlContent);
        isMarkdownMode = false;
    }
}

function previewMarkdown(editor) {
    let content;
    if (isMarkdownMode && markdownTextarea) {
        content = markdownToHtml(markdownTextarea.value);
    } else {
        content = editor.getContent();
    }
    
    // 在新窗口中预览
    const previewWindow = window.open('', '_blank');
    previewWindow.document.write(`
        <html>
            <head><title>预览</title></head>
            <body style="font-family: Arial, sans-serif; padding: 20px;">
                ${content}
            </body>
        </html>
    `);
}
```

#### 17.2.3 方案三：第三方 Markdown 插件

使用社区开发的 Markdown 插件：

```javascript
// 示例：使用 tinymce-markdown 插件
// npm install tinymce-markdown

import 'tinymce-markdown';

tinymce.init({
    selector: '#mytextarea',
    plugins: 'markdown',
    toolbar: 'markdown',
    
    // Markdown 插件配置
    markdown: {
        // 启用实时预览
        preview: true,
        // 自定义转换选项
        marked_options: {
            breaks: true,
            gfm: true
        }
    }
});
```

### 17.3 TinyMCE 数据传输格式

#### 17.3.1 富文本内容传输格式

TinyMCE 富文本编辑器的内容**主要以 HTML 字符串格式**传递给后端，而不是 JSON 格式。但在实际应用中，通常会将 HTML 内容包装在 JSON 对象中进行传输。

**基本传输格式：**

```javascript
// 获取编辑器内容
const content = tinymce.get('mytextarea').getContent();

// 包装成 JSON 格式传输
const data = {
    title: '文章标题',
    content: content,  // HTML 字符串
    author: '作者',
    createTime: new Date().toISOString()
};

// 发送到后端
fetch('/api/article/save', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
});
```

#### 17.3.2 常见的 JSON 数据格式

**1. 基础文章格式：**

```json
{
  "id": "123456",
  "title": "文章标题",
  "content": "<p>这是富文本内容，包含<strong>粗体</strong>和<em>斜体</em></p>",
  "summary": "文章摘要",
  "author": "作者姓名",
  "status": 1,
  "createTime": "2024-01-01T10:00:00Z",
  "updateTime": "2024-01-01T10:00:00Z"
}
```

**2. 扩展格式（包含元数据）：**

```json
{
  "id": "123456",
  "title": "文章标题",
  "content": "<p>富文本内容</p>",
  "contentType": "html",
  "metadata": {
    "wordCount": 1500,
    "readTime": 5,
    "tags": ["技术", "教程"],
    "category": "开发",
    "featured": true
  },
  "seo": {
    "metaTitle": "SEO标题",
    "metaDescription": "SEO描述",
    "keywords": ["关键词1", "关键词2"]
  },
  "author": {
    "id": "author123",
    "name": "作者姓名",
    "avatar": "https://example.com/avatar.jpg"
  },
  "status": "published",
  "publishTime": "2024-01-01T10:00:00Z",
  "createTime": "2024-01-01T09:00:00Z",
  "updateTime": "2024-01-01T10:00:00Z"
}
```

**3. 多语言格式：**

```json
{
  "id": "123456",
  "translations": {
    "zh-CN": {
      "title": "中文标题",
      "content": "<p>中文富文本内容</p>",
      "summary": "中文摘要"
    },
    "en-US": {
      "title": "English Title",
      "content": "<p>English rich text content</p>",
      "summary": "English summary"
    }
  },
  "defaultLanguage": "zh-CN",
  "author": "作者姓名",
  "status": "published",
  "createTime": "2024-01-01T10:00:00Z"
}
```

**4. 版本控制格式：**

```json
{
  "id": "123456",
  "currentVersion": {
    "version": "1.2",
    "title": "当前标题",
    "content": "<p>当前富文本内容</p>",
    "author": "编辑者",
    "updateTime": "2024-01-01T10:00:00Z",
    "changeLog": "修改了段落格式"
  },
  "versions": [
    {
      "version": "1.1",
      "title": "历史标题",
      "content": "<p>历史富文本内容</p>",
      "author": "原作者",
      "updateTime": "2024-01-01T09:00:00Z",
      "changeLog": "初始版本"
    }
  ],
  "status": "published"
}
```

#### 17.3.3 内容格式扩展

**1. 支持多种内容格式：**

```json
{
  "id": "123456",
  "title": "文章标题",
  "contentFormats": {
    "html": "<p>HTML格式内容</p>",
    "markdown": "# Markdown格式内容\n\n这是**粗体**文本",
    "plainText": "纯文本格式内容",
    "json": {
      "blocks": [
        {
          "type": "paragraph",
          "data": {
            "text": "结构化内容块"
          }
        }
      ]
    }
  },
  "primaryFormat": "html",
  "author": "作者姓名",
  "createTime": "2024-01-01T10:00:00Z"
}
```

**2. 富媒体内容扩展：**

```json
{
  "id": "123456",
  "title": "富媒体文章",
  "content": "<p>文章内容</p>",
  "attachments": [
    {
      "id": "att001",
      "type": "image",
      "url": "https://example.com/image.jpg",
      "alt": "图片描述",
      "caption": "图片标题",
      "size": 1024000,
      "dimensions": {
        "width": 1920,
        "height": 1080
      }
    },
    {
      "id": "att002",
      "type": "video",
      "url": "https://example.com/video.mp4",
      "thumbnail": "https://example.com/thumb.jpg",
      "duration": 120,
      "size": 50000000
    },
    {
      "id": "att003",
      "type": "document",
      "url": "https://example.com/doc.pdf",
      "filename": "document.pdf",
      "size": 2048000,
      "mimeType": "application/pdf"
    }
  ],
  "embeds": [
    {
      "type": "youtube",
      "videoId": "dQw4w9WgXcQ",
      "title": "视频标题",
      "thumbnail": "https://img.youtube.com/vi/dQw4w9WgXcQ/maxresdefault.jpg"
    },
    {
      "type": "code",
      "language": "javascript",
      "code": "console.log('Hello World');",
      "title": "代码示例"
    }
  ]
}
```

#### 17.3.4 自定义扩展类型

**1. 业务特定扩展：**

```json
{
  "id": "123456",
  "title": "产品介绍",
  "content": "<p>产品描述</p>",
  "businessData": {
    "productId": "prod001",
    "price": 99.99,
    "currency": "CNY",
    "inventory": 100,
    "specifications": {
      "color": "红色",
      "size": "L",
      "weight": "500g"
    },
    "relatedProducts": ["prod002", "prod003"]
  },
  "workflow": {
    "currentStage": "review",
    "assignee": "reviewer001",
    "deadline": "2024-01-15T10:00:00Z",
    "approvalHistory": [
      {
        "stage": "draft",
        "user": "author001",
        "action": "submit",
        "timestamp": "2024-01-01T10:00:00Z",
        "comment": "提交审核"
      }
    ]
  }
}
```

**2. 扩展字段配置：**

```javascript
// 前端扩展配置
const extendedConfig = {
    // 自定义字段映射
    customFields: {
        businessType: 'string',
        priority: 'number',
        tags: 'array',
        metadata: 'object'
    },
    
    // 数据验证规则
    validation: {
        title: { required: true, maxLength: 200 },
        content: { required: true, minLength: 10 },
        businessType: { enum: ['article', 'product', 'news'] }
    },
    
    // 数据转换器
    transformers: {
        beforeSave: (data) => {
            // 保存前数据处理
            data.wordCount = countWords(data.content);
            data.lastModified = new Date().toISOString();
            return data;
        },
        afterLoad: (data) => {
            // 加载后数据处理
            data.displayContent = sanitizeHtml(data.content);
            return data;
        }
    }
};
```

**3. 后端扩展处理：**

```java
/**
 * 文章实体扩展
 */
@Entity
@Table(name = "articles")
public class Article {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;
    
    // 扩展字段 - 使用JSON存储
    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> metadata;
    
    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonConverter.class)
    private List<Attachment> attachments;
    
    @Column(columnDefinition = "JSON")
    @Convert(converter = JsonConverter.class)
    private BusinessData businessData;
    
    // 标准字段
    private String author;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // getter/setter...
}

/**
 * 业务数据扩展
 */
@Data
public class BusinessData {
    private String businessType;
    private Integer priority;
    private List<String> tags;
    private Map<String, Object> customFields;
}

/**
 * 附件信息
 */
@Data
public class Attachment {
    private String id;
    private String type;  // image, video, document, etc.
    private String url;
    private String filename;
    private Long size;
    private Map<String, Object> properties;
}
```

### 17.4 文件上传 JSON 格式规范

#### 17.3.1 成功响应格式

当文件上传成功时，后端必须返回以下 JSON 格式：

```json
{
  "location": "https://example.com/uploads/image.jpg"
}
```

**字段说明：**
- `location`：上传文件的完整访问URL，必须是可直接访问的链接

#### 17.3.2 错误响应格式

当文件上传失败时，后端必须返回以下 JSON 格式：

```json
{
  "error": "文件上传失败的具体错误信息"
}
```

**字段说明：**
- `error`：错误描述信息，将显示给用户

#### 17.3.3 完整的上传配置示例

```javascript
tinymce.init({
    selector: '#mytextarea',
    plugins: 'image media link',
    toolbar: 'image media link',
    
    // 方式1：使用 images_upload_url（推荐）
    images_upload_url: '/api/upload/image',
    images_upload_base_path: '/uploads/',
    images_upload_credentials: true,
    
    // 方式2：使用 images_upload_handler（更灵活）
    images_upload_handler: function (blobInfo, success, failure, progress) {
        const formData = new FormData();
        formData.append('file', blobInfo.blob(), blobInfo.filename());
        
        // 添加额外参数
        formData.append('type', 'image');
        formData.append('folder', 'tinymce');
        
        fetch('/api/upload/image', {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(result => {
            // 检查响应格式
            if (result.location) {
                success(result.location);
            } else if (result.error) {
                failure(result.error);
            } else {
                failure('服务器返回了无效的响应格式');
            }
        })
        .catch(error => {
            failure('上传失败：' + error.message);
        });
    },
    
    // 文件选择器配置
    file_picker_callback: function (callback, value, meta) {
        const input = document.createElement('input');
        input.setAttribute('type', 'file');
        
        // 根据类型设置接受的文件格式
        if (meta.filetype === 'image') {
            input.setAttribute('accept', 'image/*');
        } else if (meta.filetype === 'media') {
            input.setAttribute('accept', 'video/*,audio/*');
        } else {
            input.setAttribute('accept', '*/*');
        }
        
        input.onchange = function () {
            const file = this.files[0];
            
            // 文件大小检查（例如：限制为 10MB）
            if (file.size > 10 * 1024 * 1024) {
                alert('文件大小不能超过 10MB');
                return;
            }
            
            const reader = new FileReader();
            reader.onload = function () {
                const id = 'blobid' + (new Date()).getTime();
                const blobCache = tinymce.activeEditor.editorUpload.blobCache;
                const base64 = reader.result.split(',')[1];
                const blobInfo = blobCache.create(id, file, base64);
                blobCache.add(blobInfo);
                
                callback(blobInfo.blobUri(), { title: file.name });
            };
            reader.readAsDataURL(file);
        };
        
        input.click();
    }
});
```

#### 17.3.4 后端实现示例

**Spring Boot 示例：**

```java
@RestController
@RequestMapping("/api/upload")
public class UploadController {
    
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 文件验证
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "文件不能为空"));
            }
            
            // 文件类型检查
            String contentType = file.getContentType();
            if (!contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "只允许上传图片文件"));
            }
            
            // 文件大小检查
            if (file.getSize() > 10 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "文件大小不能超过 10MB"));
            }
            
            // 保存文件
            String fileName = UUID.randomUUID().toString() + 
                getFileExtension(file.getOriginalFilename());
            String filePath = "/uploads/" + fileName;
            
            // 实际保存逻辑
            saveFile(file, filePath);
            
            // 返回成功响应
            String fileUrl = "https://yourdomain.com" + filePath;
            return ResponseEntity.ok(Map.of("location", fileUrl));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "文件上传失败：" + e.getMessage()));
        }
    }
    
    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    private void saveFile(MultipartFile file, String path) throws IOException {
        // 实现文件保存逻辑
        Files.copy(file.getInputStream(), Paths.get(path));
    }
}
```

### 17.4 Markdown 最佳实践

#### 17.4.1 选择合适的方案

1. **轻量级需求**：使用转换库在保存/加载时进行格式转换
2. **重度 Markdown 使用**：考虑使用专门的 Markdown 编辑器
3. **混合需求**：实现双模式切换功能

#### 17.4.2 推荐的 Markdown 编辑器替代方案

如果项目主要使用 Markdown，建议考虑以下专门的编辑器：

- **Editor.js**：块级编辑器，支持 Markdown 导出
- **CodeMirror**：代码编辑器，有 Markdown 模式
- **Monaco Editor**：VS Code 的编辑器，支持 Markdown
- **Typora**：专业的 Markdown 编辑器（桌面应用）

#### 17.4.3 集成建议

```javascript
// 推荐的集成方式
class MarkdownTinyMCE {
    constructor(selector, options = {}) {
        this.selector = selector;
        this.options = {
            enableMarkdown: true,
            autoSave: true,
            ...options
        };
        this.init();
    }
    
    init() {
        tinymce.init({
            selector: this.selector,
            ...this.getDefaultConfig(),
            ...this.options.tinymceConfig
        });
    }
    
    getDefaultConfig() {
        return {
            plugins: 'code preview',
            toolbar: 'markdown-toggle | bold italic | code preview',
            setup: (editor) => {
                if (this.options.enableMarkdown) {
                    this.setupMarkdownSupport(editor);
                }
            }
        };
    }
    
    setupMarkdownSupport(editor) {
        // 实现 Markdown 支持逻辑
    }
}

// 使用示例
const editor = new MarkdownTinyMCE('#mytextarea', {
    enableMarkdown: true,
    autoSave: true,
    tinymceConfig: {
        height: 500
    }
});
```

### 16.3 参考资源

- [TinyMCE官方文档](https://www.tiny.cloud/docs/)
- [TinyMCE GitHub仓库](https://github.com/tinymce/tinymce)
- [TinyMCE社区论坛](https://community.tiny.cloud/)
- [TinyMCE示例集合](https://www.tiny.cloud/docs/tinymce/latest/examples/)
- [Marked.js - Markdown解析器](https://marked.js.org/)
- [Turndown - HTML转Markdown](https://github.com/mixmark-io/turndown)
- [CodeMirror - 代码编辑器](https://codemirror.net/)

---

*本文档基于TinyMCE 8版本编写，内容会随着版本更新而调整。建议定期查看官方文档获取最新信息。*