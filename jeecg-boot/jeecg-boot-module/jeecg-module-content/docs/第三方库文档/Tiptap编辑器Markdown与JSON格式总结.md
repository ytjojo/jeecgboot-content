# Tiptap编辑器 Markdown与JSON格式总结

## 概述

Tiptap是一个基于ProseMirror的现代富文本编辑器，支持Markdown格式的导入导出以及JSON格式的数据存储。本文档总结了Tiptap中主要Node和Mark扩展的Markdown格式和对应的JSON格式。

## Node扩展

### 1. Paragraph（段落）

**Markdown格式：**
```markdown
这是一个段落。

这是另一个段落。
```

**JSON格式：**
```json
{
  "type": "paragraph",
  "content": [
    {
      "type": "text",
      "text": "这是一个段落。"
    }
  ]
}
```

### 2. Heading（标题）

**Markdown格式：**
```markdown
# 一级标题
## 二级标题
### 三级标题
#### 四级标题
##### 五级标题
###### 六级标题
```

**JSON格式：**
```json
{
  "type": "heading",
  "attrs": {
    "level": 1
  },
  "content": [
    {
      "type": "text",
      "text": "一级标题"
    }
  ]
}
```

### 3. Blockquote（引用）

**Markdown格式：**
```markdown
> 这是一个引用
> 可以有多行
```

**JSON格式：**
```json
{
  "type": "blockquote",
  "content": [
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "这是一个引用\n可以有多行"
        }
      ]
    }
  ]
}
```

### 4. CodeBlock（代码块）

**Markdown格式：**
```markdown
```javascript
function hello() {
  console.log('Hello World!');
}
```
```

**JSON格式：**
```json
{
  "type": "codeBlock",
  "attrs": {
    "language": "javascript"
  },
  "content": [
    {
      "type": "text",
      "text": "function hello() {\n  console.log('Hello World!');\n}"
    }
  ]
}
```

### 5. BulletList（无序列表）

**Markdown格式：**
```markdown
- 第一项
- 第二项
  - 嵌套项
- 第三项
```

**JSON格式：**
```json
{
  "type": "bulletList",
  "content": [
    {
      "type": "listItem",
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "第一项"
            }
          ]
        }
      ]
    },
    {
      "type": "listItem",
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "第二项"
            }
          ]
        },
        {
          "type": "bulletList",
          "content": [
            {
              "type": "listItem",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "嵌套项"
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

### 6. OrderedList（有序列表）

**Markdown格式：**
```markdown
1. 第一项
2. 第二项
3. 第三项
```

**JSON格式：**
```json
{
  "type": "orderedList",
  "attrs": {
    "start": 1
  },
  "content": [
    {
      "type": "listItem",
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "第一项"
            }
          ]
        }
      ]
    }
  ]
}
```

### 7. HardBreak（强制换行）

**Markdown格式：**
```markdown
第一行  
第二行
```

**JSON格式：**
```json
{
  "type": "hardBreak"
}
```

### 8. HorizontalRule（水平分割线）

**Markdown格式：**
```markdown
---
```

**JSON格式：**
```json
{
  "type": "horizontalRule"
}
```

### 9. Image（图片）

**Markdown格式：**
```markdown
![Alt text](https://example.com/image.jpg "Title")
```

**JSON格式：**
```json
{
  "type": "image",
  "attrs": {
    "src": "https://example.com/image.jpg",
    "alt": "Alt text",
    "title": "Title"
  }
}
```

### 10. Table（表格）

**Markdown格式：**
```markdown
| 标题1 | 标题2 | 标题3 |
|-------|-------|-------|
| 内容1 | 内容2 | 内容3 |
| 内容4 | 内容5 | 内容6 |
```

**JSON格式：**
```json
{
  "type": "table",
  "content": [
    {
      "type": "tableRow",
      "content": [
        {
          "type": "tableHeader",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "标题1"
                }
              ]
            }
          ]
        },
        {
          "type": "tableHeader",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "标题2"
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "tableRow",
      "content": [
        {
          "type": "tableCell",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "内容1"
                }
              ]
            }
          ]
        },
        {
          "type": "tableCell",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "内容2"
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```

### 11. TaskList（任务列表）

**Markdown格式：**
```markdown
- [x] 已完成任务
- [ ] 未完成任务
- [x] 另一个已完成任务
```

**JSON格式：**
```json
{
  "type": "taskList",
  "content": [
    {
      "type": "taskItem",
      "attrs": {
        "checked": true
      },
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "已完成任务"
            }
          ]
        }
      ]
    },
    {
      "type": "taskItem",
      "attrs": {
        "checked": false
      },
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "未完成任务"
            }
          ]
        }
      ]
    }
  ]
}
```

### 12. Mathematics（数学公式）

**Markdown格式：**
```markdown
行内公式：$E = mc^2$

块级公式：
$$
\int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
$$
```

**JSON格式：**
```json
{
  "type": "mathBlock",
  "attrs": {
    "latex": "\\int_{-\\infty}^{\\infty} e^{-x^2} dx = \\sqrt{\\pi}"
  }
}
```

### 13. Mention（提及）

**Markdown格式：**
```markdown
@username
```

**JSON格式：**
```json
{
  "type": "mention",
  "attrs": {
    "id": "user123",
    "label": "username"
  }
}
```

### 14. Youtube（YouTube视频）

**Markdown格式：**
```markdown
[YouTube](https://www.youtube.com/watch?v=dQw4w9WgXcQ)
```

**JSON格式：**
```json
{
  "type": "youtube",
  "attrs": {
    "src": "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    "width": 640,
    "height": 480
  }
}
```

### 15. Details（折叠详情）

**Markdown格式：**
```markdown
<details>
<summary>点击展开</summary>
这里是详细内容
</details>
```

**JSON格式：**
```json
{
  "type": "details",
  "content": [
    {
      "type": "detailsSummary",
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "点击展开"
            }
          ]
        }
      ]
    },
    {
      "type": "detailsContent",
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "这里是详细内容"
            }
          ]
        }
      ]
    }
  ]
}
```

### 16. Emoji（表情符号）

**Markdown格式：**
```markdown
:smile: :heart: :thumbsup:
```

**JSON格式：**
```json
{
  "type": "emoji",
  "attrs": {
    "name": "smile",
    "emoji": "😄",
    "fallbackImage": "https://example.com/emoji/smile.png"
  }
}
```

## Mark扩展

### 1. Bold（粗体）

**Markdown格式：**
```markdown
**粗体文本** 或 __粗体文本__
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "粗体文本",
  "marks": [
    {
      "type": "bold"
    }
  ]
}
```

### 2. Italic（斜体）

**Markdown格式：**
```markdown
*斜体文本* 或 _斜体文本_
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "斜体文本",
  "marks": [
    {
      "type": "italic"
    }
  ]
}
```

### 3. Code（行内代码）

**Markdown格式：**
```markdown
`行内代码`
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "行内代码",
  "marks": [
    {
      "type": "code"
    }
  ]
}
```

### 4. Link（链接）

**Markdown格式：**
```markdown
[链接文本](https://example.com)
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "链接文本",
  "marks": [
    {
      "type": "link",
      "attrs": {
        "href": "https://example.com",
        "target": "_blank"
      }
    }
  ]
}
```

### 5. Strike（删除线）

**Markdown格式：**
```markdown
~~删除线文本~~
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "删除线文本",
  "marks": [
    {
      "type": "strike"
    }
  ]
}
```

### 6. Underline（下划线）

**Markdown格式：**
```markdown
<u>下划线文本</u>
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "下划线文本",
  "marks": [
    {
      "type": "underline"
    }
  ]
}
```

### 7. Superscript（上标）

**Markdown格式：**
```markdown
E=mc<sup>2</sup>
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "2",
  "marks": [
    {
      "type": "superscript"
    }
  ]
}
```

### 8. Subscript（下标）

**Markdown格式：**
```markdown
H<sub>2</sub>O
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "2",
  "marks": [
    {
      "type": "subscript"
    }
  ]
}
```

### 9. Highlight（高亮）

**Markdown格式：**
```markdown
==高亮文本==
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "高亮文本",
  "marks": [
    {
      "type": "highlight",
      "attrs": {
        "color": "#ffff00"
      }
    }
  ]
}
```

### 10. TextStyle（文本样式）

**Markdown格式：**
```markdown
<!-- TextStyle通常不直接支持Markdown，需要通过HTML标签 -->
<span style="color: red; font-size: 18px;">红色大字</span>
```

**JSON格式：**
```json
{
  "type": "text",
  "text": "红色大字",
  "marks": [
    {
      "type": "textStyle",
      "attrs": {
        "color": "red",
        "fontSize": "18px",
        "fontFamily": "Arial",
        "fontWeight": "bold"
      }
    }
  ]
}
```

## 完整文档示例

### Markdown示例
```markdown
# 主标题

## 二级标题

这是一个**粗体文本**和*斜体文本*的段落。

> 这是一个引用块
> 可以包含多行内容

- 无序列表项1
- 无序列表项2
  - 嵌套列表项

1. 有序列表项1
2. 有序列表项2

- [x] 已完成任务
- [ ] 未完成任务

这里有一个`行内代码`示例。

```javascript
// 代码块示例
function hello() {
    console.log("Hello World!");
}
```

这里是一个[链接](https://example.com)。

![图片示例](https://example.com/image.jpg "图片标题")

| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 数据1 | 数据2 | 数据3 |
| 数据4 | 数据5 | 数据6 |

---

~~删除线文本~~和<u>下划线文本</u>。

==高亮文本==

E = mc<sup>2</sup> 和 H<sub>2</sub>O

行内数学公式：$E = mc^2$

块级数学公式：
$$
\int_{-\infty}^{\infty} e^{-x^2} dx = \sqrt{\pi}
$$

@username 提及用户

:smile: :heart: 表情符号

<details>
<summary>点击展开详情</summary>
这里是折叠的详细内容
</details>

[YouTube视频](https://www.youtube.com/watch?v=dQw4w9WgXcQ)
```

### JSON示例
```json
{
  "type": "doc",
  "content": [
    {
      "type": "heading",
      "attrs": {
        "level": 1
      },
      "content": [
        {
          "type": "text",
          "text": "主标题"
        }
      ]
    },
    {
      "type": "heading",
      "attrs": {
        "level": 2
      },
      "content": [
        {
          "type": "text",
          "text": "二级标题"
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "这是一个"
        },
        {
          "type": "text",
          "text": "粗体文本",
          "marks": [
            {
              "type": "bold"
            }
          ]
        },
        {
          "type": "text",
          "text": "和"
        },
        {
          "type": "text",
          "text": "斜体文本",
          "marks": [
            {
              "type": "italic"
            }
          ]
        },
        {
          "type": "text",
          "text": "的段落。"
        }
      ]
    },
    {
      "type": "blockquote",
      "content": [
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "这是一个引用块"
            }
          ]
        },
        {
          "type": "paragraph",
          "content": [
            {
              "type": "text",
              "text": "可以包含多行内容"
            }
          ]
        }
      ]
    },
    {
      "type": "bulletList",
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "无序列表项1"
                }
              ]
            }
          ]
        },
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "无序列表项2"
                }
              ]
            },
            {
              "type": "bulletList",
              "content": [
                {
                  "type": "listItem",
                  "content": [
                    {
                      "type": "paragraph",
                      "content": [
                        {
                          "type": "text",
                          "text": "嵌套列表项"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "orderedList",
      "attrs": {
        "start": 1
      },
      "content": [
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "有序列表项1"
                }
              ]
            }
          ]
        },
        {
          "type": "listItem",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "有序列表项2"
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "taskList",
      "content": [
        {
          "type": "taskItem",
          "attrs": {
            "checked": true
          },
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "已完成任务"
                }
              ]
            }
          ]
        },
        {
          "type": "taskItem",
          "attrs": {
            "checked": false
          },
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "未完成任务"
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "这里有一个"
        },
        {
          "type": "text",
          "text": "行内代码",
          "marks": [
            {
              "type": "code"
            }
          ]
        },
        {
          "type": "text",
          "text": "示例。"
        }
      ]
    },
    {
      "type": "codeBlock",
      "attrs": {
        "language": "javascript"
      },
      "content": [
        {
          "type": "text",
          "text": "// 代码块示例\nfunction hello() {\n    console.log(\"Hello World!\");\n}"
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "这里是一个"
        },
        {
          "type": "text",
          "text": "链接",
          "marks": [
            {
              "type": "link",
              "attrs": {
                "href": "https://example.com"
              }
            }
          ]
        },
        {
          "type": "text",
          "text": "。"
        }
      ]
    },
    {
      "type": "image",
      "attrs": {
        "src": "https://example.com/image.jpg",
        "alt": "图片示例",
        "title": "图片标题"
      }
    },
    {
      "type": "table",
      "content": [
        {
          "type": "tableRow",
          "content": [
            {
              "type": "tableHeader",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "列1"
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableHeader",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "列2"
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableHeader",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "列3"
                    }
                  ]
                }
              ]
            }
          ]
        },
        {
          "type": "tableRow",
          "content": [
            {
              "type": "tableCell",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "数据1"
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableCell",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "数据2"
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableCell",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "数据3"
                    }
                  ]
                }
              ]
            }
          ]
        },
        {
          "type": "tableRow",
          "content": [
            {
              "type": "tableCell",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "数据4"
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableCell",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "数据5"
                    }
                  ]
                }
              ]
            },
            {
              "type": "tableCell",
              "content": [
                {
                  "type": "paragraph",
                  "content": [
                    {
                      "type": "text",
                      "text": "数据6"
                    }
                  ]
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "horizontalRule"
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "删除线文本",
          "marks": [
            {
              "type": "strike"
            }
          ]
        },
        {
          "type": "text",
          "text": "和"
        },
        {
          "type": "text",
          "text": "下划线文本",
          "marks": [
            {
              "type": "underline"
            }
          ]
        },
        {
          "type": "text",
          "text": "。"
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "高亮文本",
          "marks": [
            {
              "type": "highlight"
            }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "E = mc"
        },
        {
          "type": "text",
          "text": "2",
          "marks": [
            {
              "type": "superscript"
            }
          ]
        },
        {
          "type": "text",
          "text": " 和 H"
        },
        {
          "type": "text",
          "text": "2",
          "marks": [
            {
              "type": "subscript"
            }
          ]
        },
        {
          "type": "text",
          "text": "O"
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "行内数学公式："
        },
        {
          "type": "text",
          "text": "$E = mc^2$",
          "marks": [
            {
              "type": "mathInline",
              "attrs": {
                "latex": "E = mc^2"
              }
            }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "块级数学公式："
        }
      ]
    },
    {
      "type": "mathBlock",
      "attrs": {
        "latex": "\\int_{-\\infty}^{\\infty} e^{-x^2} dx = \\sqrt{\\pi}"
      }
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "mention",
          "attrs": {
            "id": "user123",
            "label": "username"
          }
        },
        {
          "type": "text",
          "text": " 提及用户"
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "emoji",
          "attrs": {
            "name": "smile",
            "emoji": "😄"
          }
        },
        {
          "type": "text",
          "text": " "
        },
        {
          "type": "emoji",
          "attrs": {
            "name": "heart",
            "emoji": "❤️"
          }
        },
        {
          "type": "text",
          "text": " 表情符号"
        }
      ]
    },
    {
      "type": "details",
      "content": [
        {
          "type": "detailsSummary",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "点击展开详情"
                }
              ]
            }
          ]
        },
        {
          "type": "detailsContent",
          "content": [
            {
              "type": "paragraph",
              "content": [
                {
                  "type": "text",
                  "text": "这里是折叠的详细内容"
                }
              ]
            }
          ]
        }
      ]
    },
    {
      "type": "paragraph",
      "content": [
        {
          "type": "text",
          "text": "YouTube视频",
          "marks": [
            {
              "type": "link",
              "attrs": {
                "href": "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
              }
            }
          ]
        }
      ]
    }
  ]
}
```

## 使用注意事项

### 基础注意事项
1. **版本兼容性**：不同版本的Tiptap可能在JSON格式上有细微差异
2. **扩展依赖**：某些Node和Mark需要安装对应的扩展包
3. **自定义属性**：可以根据需要添加自定义属性到attrs对象中
4. **嵌套结构**：注意JSON中的嵌套关系，特别是列表和引用的处理
5. **文本节点**：所有文本内容都需要包装在text类型的节点中
6. **标记组合**：多个mark可以同时应用到同一个文本节点上

### 扩展特定注意事项

#### Table扩展
- 表格必须包含tableRow、tableHeader和tableCell节点
- 每个单元格内容需要包装在paragraph中
- 表格结构必须保持完整性

#### Mathematics扩展
- 需要安装@tiptap/extension-mathematics扩展
- LaTeX语法中的反斜杠需要双重转义
- 支持行内公式和块级公式两种模式

#### Image扩展
- src属性是必需的
- alt和title属性是可选的
- 支持本地图片和网络图片

#### TaskList扩展
- 任务项的checked属性控制复选框状态
- 任务列表可以嵌套在其他列表中

#### Mention扩展
- 需要提供用户ID和显示标签
- 通常与用户系统集成使用

#### Details扩展
- 必须包含detailsSummary和detailsContent两部分
- 支持嵌套其他内容类型

#### Emoji扩展
- 可以使用emoji名称或直接使用Unicode字符
- 支持fallback图片机制

#### Highlight扩展
- 支持自定义高亮颜色
- 可以与其他文本标记组合使用

#### TextStyle扩展
- 支持字体、颜色、大小等样式属性
- 通常用于富文本编辑场景

### JSON结构说明

1. **JSON结构**：Tiptap的JSON格式基于ProseMirror的文档模型，每个节点都有`type`属性，可选的`attrs`属性用于存储节点属性，`content`数组包含子节点。

2. **Marks应用**：Marks通过`marks`数组应用到文本节点上，可以同时应用多个marks。

3. **扩展性**：Tiptap支持自定义扩展，可以创建自定义的nodes和marks，并定义它们的Markdown解析和JSON序列化规则。

4. **转换工具**：Tiptap提供了内置的Markdown扩展，可以实现Markdown和JSON格式之间的相互转换。

## 参考资料

### 官方文档
1. [Tiptap官方文档](https://tiptap.dev/)
2. [Tiptap Node扩展](https://tiptap.dev/docs/editor/extensions/nodes)
3. [Tiptap Mark扩展](https://tiptap.dev/docs/editor/extensions/marks)
4. [Tiptap Markdown扩展](https://tiptap.dev/api/extensions/markdown)
5. [ProseMirror文档模型](https://prosemirror.net/docs/guide/#doc)
6. [Tiptap JSON格式说明](https://tiptap.dev/guide/output)

### 扩展文档
1. [Table扩展](https://tiptap.dev/api/nodes/table)
2. [Image扩展](https://tiptap.dev/api/nodes/image)
3. [TaskList扩展](https://tiptap.dev/api/nodes/task-list)
4. [Mathematics扩展](https://tiptap.dev/api/nodes/mathematics)
5. [Mention扩展](https://tiptap.dev/api/nodes/mention)
6. [Youtube扩展](https://tiptap.dev/api/nodes/youtube)
7. [Details扩展](https://tiptap.dev/api/nodes/details)
8. [Emoji扩展](https://tiptap.dev/api/nodes/emoji)
9. [Highlight扩展](https://tiptap.dev/api/marks/highlight)
10. [TextStyle扩展](https://tiptap.dev/api/marks/text-style)

### 开发资源
1. [Tiptap GitHub仓库](https://github.com/ueberdosis/tiptap)
2. [Tiptap示例集合](https://tiptap.dev/examples)
3. [自定义扩展开发指南](https://tiptap.dev/guide/custom-extensions)

---

*本文档基于Tiptap v2.x版本整理，涵盖了完整的Node和Mark扩展列表。如有更新请参考官方最新文档。*