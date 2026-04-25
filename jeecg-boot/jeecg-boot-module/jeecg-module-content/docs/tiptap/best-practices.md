# Tiptap 最佳实践和常见问题解决方案

## 概述

本文档汇总了 Tiptap 编辑器开发中的最佳实践、性能优化技巧和常见问题的解决方案，帮助开发者构建高质量的富文本编辑器应用。

## 最佳实践

### 1. 编辑器初始化

#### 1.1 延迟初始化

```javascript
// ❌ 不推荐：立即初始化可能影响页面加载性能
const editor = new Editor({
  element: document.querySelector('.editor'),
  extensions: [StarterKit],
  content: largeContent,
})

// ✅ 推荐：延迟初始化
const initEditor = () => {
  return new Promise((resolve) => {
    setTimeout(() => {
      const editor = new Editor({
        element: document.querySelector('.editor'),
        extensions: [StarterKit],
        content: '',
        onReady: () => {
          resolve(editor)
        },
      })
    }, 100)
  })
}

// 在需要时初始化
const editor = await initEditor()
```

#### 1.2 条件加载扩展

```javascript
/**
 * 根据功能需求动态加载扩展
 */
const createEditor = (features = {}) => {
  const extensions = [Document, Paragraph, Text]

  // 基础格式化
  if (features.formatting) {
    extensions.push(Bold, Italic, Strike)
  }

  // 列表功能
  if (features.lists) {
    extensions.push(BulletList, OrderedList, ListItem)
  }

  // 表格功能
  if (features.tables) {
    extensions.push(Table, TableRow, TableHeader, TableCell)
  }

  // 协作功能
  if (features.collaboration) {
    extensions.push(
      Collaboration.configure({
        document: features.collaborationDoc,
      }),
      CollaborationCursor.configure({
        provider: features.collaborationProvider,
      })
    )
  }

  return new Editor({
    extensions,
    content: features.initialContent || '',
  })
}

// 使用示例
const editor = createEditor({
  formatting: true,
  lists: true,
  tables: false,
  collaboration: false,
})
```

### 2. 性能优化

#### 2.1 内容分片加载

```javascript
/**
 * 大文档分片加载工具类
 */
class ContentLoader {
  constructor(editor, options = {}) {
    this.editor = editor
    this.chunkSize = options.chunkSize || 1000 // 每片字符数
    this.loadDelay = options.loadDelay || 50 // 加载延迟（毫秒）
  }

  /**
   * 分片加载大文档
   * @param {string} content - 完整内容
   */
  async loadLargeContent(content) {
    const chunks = this.splitContent(content)
    
    // 先加载第一片
    this.editor.commands.setContent(chunks[0] || '')
    
    // 逐步加载剩余内容
    for (let i = 1; i < chunks.length; i++) {
      await this.delay(this.loadDelay)
      this.editor.commands.insertContentAt(
        this.editor.state.doc.content.size,
        chunks[i]
      )
    }
  }

  /**
   * 分割内容
   * @param {string} content - 原始内容
   * @returns {string[]} 内容片段数组
   */
  splitContent(content) {
    const chunks = []
    for (let i = 0; i < content.length; i += this.chunkSize) {
      chunks.push(content.slice(i, i + this.chunkSize))
    }
    return chunks
  }

  /**
   * 延迟工具方法
   * @param {number} ms - 延迟毫秒数
   */
  delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms))
  }
}

// 使用示例
const contentLoader = new ContentLoader(editor, {
  chunkSize: 2000,
  loadDelay: 100,
})

contentLoader.loadLargeContent(largeDocumentContent)
```

#### 2.2 虚拟滚动优化

```javascript
/**
 * 虚拟滚动扩展（适用于长文档）
 */
import { Extension } from '@tiptap/core'
import { Plugin, PluginKey } from '@tiptap/pm/state'
import { Decoration, DecorationSet } from '@tiptap/pm/view'

export const VirtualScroll = Extension.create({
  name: 'virtualScroll',

  addOptions() {
    return {
      itemHeight: 20, // 每行高度
      containerHeight: 400, // 容器高度
      overscan: 5, // 预渲染行数
    }
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: new PluginKey('virtualScroll'),
        props: {
          decorations: (state) => {
            return this.createVirtualDecorations(state)
          },
        },
      }),
    ]
  },

  createVirtualDecorations(state) {
    const { itemHeight, containerHeight, overscan } = this.options
    const visibleCount = Math.ceil(containerHeight / itemHeight)
    const scrollTop = this.getScrollTop()
    const startIndex = Math.floor(scrollTop / itemHeight)
    const endIndex = Math.min(
      startIndex + visibleCount + overscan,
      state.doc.childCount
    )

    const decorations = []
    
    // 添加占位符装饰
    if (startIndex > 0) {
      decorations.push(
        Decoration.widget(0, () => {
          const div = document.createElement('div')
          div.style.height = `${startIndex * itemHeight}px`
          return div
        })
      )
    }

    return DecorationSet.create(state.doc, decorations)
  },

  getScrollTop() {
    const container = this.editor.view.dom.closest('.scroll-container')
    return container ? container.scrollTop : 0
  },
})
```

#### 2.3 防抖优化

```javascript
/**
 * 防抖工具类
 */
class EditorDebouncer {
  constructor() {
    this.timers = new Map()
  }

  /**
   * 防抖执行
   * @param {string} key - 防抖键
   * @param {Function} fn - 执行函数
   * @param {number} delay - 延迟时间
   */
  debounce(key, fn, delay = 300) {
    if (this.timers.has(key)) {
      clearTimeout(this.timers.get(key))
    }

    const timer = setTimeout(() => {
      fn()
      this.timers.delete(key)
    }, delay)

    this.timers.set(key, timer)
  }

  /**
   * 清除所有防抖
   */
  clear() {
    this.timers.forEach(timer => clearTimeout(timer))
    this.timers.clear()
  }
}

// 使用示例
const debouncer = new EditorDebouncer()

const editor = new Editor({
  extensions: [StarterKit],
  onUpdate: ({ editor }) => {
    // 防抖保存
    debouncer.debounce('save', () => {
      saveContent(editor.getHTML())
    }, 1000)

    // 防抖字数统计
    debouncer.debounce('wordCount', () => {
      updateWordCount(editor.storage.characterCount.words())
    }, 500)
  },
})
```

### 3. 内存管理

#### 3.1 正确销毁编辑器

```javascript
/**
 * 编辑器生命周期管理
 */
class EditorManager {
  constructor() {
    this.editors = new Map()
    this.eventListeners = new Map()
  }

  /**
   * 创建编辑器
   * @param {string} id - 编辑器ID
   * @param {Object} options - 编辑器选项
   */
  createEditor(id, options) {
    // 如果已存在，先销毁
    if (this.editors.has(id)) {
      this.destroyEditor(id)
    }

    const editor = new Editor(options)
    this.editors.set(id, editor)

    // 注册事件监听器
    const listeners = {
      beforeunload: () => this.destroyEditor(id),
      visibilitychange: () => {
        if (document.hidden) {
          this.pauseEditor(id)
        } else {
          this.resumeEditor(id)
        }
      },
    }

    Object.entries(listeners).forEach(([event, handler]) => {
      window.addEventListener(event, handler)
    })

    this.eventListeners.set(id, listeners)
    return editor
  }

  /**
   * 销毁编辑器
   * @param {string} id - 编辑器ID
   */
  destroyEditor(id) {
    const editor = this.editors.get(id)
    if (editor) {
      // 清理编辑器
      editor.destroy()
      this.editors.delete(id)
    }

    // 清理事件监听器
    const listeners = this.eventListeners.get(id)
    if (listeners) {
      Object.entries(listeners).forEach(([event, handler]) => {
        window.removeEventListener(event, handler)
      })
      this.eventListeners.delete(id)
    }
  }

  /**
   * 暂停编辑器（页面不可见时）
   * @param {string} id - 编辑器ID
   */
  pauseEditor(id) {
    const editor = this.editors.get(id)
    if (editor) {
      editor.setOptions({ editable: false })
    }
  }

  /**
   * 恢复编辑器
   * @param {string} id - 编辑器ID
   */
  resumeEditor(id) {
    const editor = this.editors.get(id)
    if (editor) {
      editor.setOptions({ editable: true })
    }
  }

  /**
   * 销毁所有编辑器
   */
  destroyAll() {
    this.editors.forEach((_, id) => {
      this.destroyEditor(id)
    })
  }
}

// 使用示例
const editorManager = new EditorManager()

// 创建编辑器
const editor = editorManager.createEditor('main-editor', {
  element: document.querySelector('.editor'),
  extensions: [StarterKit],
})

// 在组件卸载时销毁
// React
useEffect(() => {
  return () => {
    editorManager.destroyEditor('main-editor')
  }
}, [])

// Vue
onUnmounted(() => {
  editorManager.destroyEditor('main-editor')
})
```

#### 3.2 内存泄漏检测

```javascript
/**
 * 内存使用监控工具
 */
class MemoryMonitor {
  constructor() {
    this.measurements = []
    this.isMonitoring = false
  }

  /**
   * 开始监控
   * @param {number} interval - 监控间隔（毫秒）
   */
  startMonitoring(interval = 5000) {
    if (this.isMonitoring) return

    this.isMonitoring = true
    this.monitorInterval = setInterval(() => {
      this.measureMemory()
    }, interval)
  }

  /**
   * 停止监控
   */
  stopMonitoring() {
    if (this.monitorInterval) {
      clearInterval(this.monitorInterval)
      this.isMonitoring = false
    }
  }

  /**
   * 测量内存使用
   */
  measureMemory() {
    if (performance.memory) {
      const measurement = {
        timestamp: Date.now(),
        used: performance.memory.usedJSHeapSize,
        total: performance.memory.totalJSHeapSize,
        limit: performance.memory.jsHeapSizeLimit,
      }

      this.measurements.push(measurement)

      // 保留最近100次测量
      if (this.measurements.length > 100) {
        this.measurements.shift()
      }

      // 检测内存泄漏
      this.detectMemoryLeak()
    }
  }

  /**
   * 检测内存泄漏
   */
  detectMemoryLeak() {
    if (this.measurements.length < 10) return

    const recent = this.measurements.slice(-10)
    const trend = this.calculateTrend(recent.map(m => m.used))

    // 如果内存使用持续增长
    if (trend > 1024 * 1024) { // 1MB
      console.warn('检测到可能的内存泄漏，内存使用持续增长')
      this.logMemoryInfo()
    }
  }

  /**
   * 计算趋势
   * @param {number[]} values - 数值数组
   */
  calculateTrend(values) {
    const n = values.length
    const sumX = (n * (n - 1)) / 2
    const sumY = values.reduce((a, b) => a + b, 0)
    const sumXY = values.reduce((sum, y, x) => sum + x * y, 0)
    const sumXX = values.reduce((sum, _, x) => sum + x * x, 0)

    return (n * sumXY - sumX * sumY) / (n * sumXX - sumX * sumX)
  }

  /**
   * 记录内存信息
   */
  logMemoryInfo() {
    const latest = this.measurements[this.measurements.length - 1]
    console.log('内存使用情况:', {
      used: `${(latest.used / 1024 / 1024).toFixed(2)} MB`,
      total: `${(latest.total / 1024 / 1024).toFixed(2)} MB`,
      usage: `${((latest.used / latest.total) * 100).toFixed(2)}%`,
    })
  }

  /**
   * 获取内存报告
   */
  getMemoryReport() {
    return {
      measurements: this.measurements,
      averageUsage: this.measurements.reduce((sum, m) => sum + m.used, 0) / this.measurements.length,
      peakUsage: Math.max(...this.measurements.map(m => m.used)),
      currentUsage: this.measurements[this.measurements.length - 1]?.used || 0,
    }
  }
}

// 使用示例
const memoryMonitor = new MemoryMonitor()
memoryMonitor.startMonitoring(3000) // 每3秒监控一次

// 在开发环境中使用
if (process.env.NODE_ENV === 'development') {
  window.memoryMonitor = memoryMonitor
  console.log('内存监控已启动，使用 window.memoryMonitor.getMemoryReport() 查看报告')
}
```

### 4. 错误处理

#### 4.1 全局错误处理

```javascript
/**
 * 编辑器错误处理器
 */
class EditorErrorHandler {
  constructor() {
    this.errorLog = []
    this.maxLogSize = 100
  }

  /**
   * 创建带错误处理的编辑器
   * @param {Object} options - 编辑器选项
   */
  createSafeEditor(options) {
    const safeOptions = {
      ...options,
      onError: (error, editor) => {
        this.handleError(error, editor)
        // 调用用户自定义的错误处理
        if (options.onError) {
          options.onError(error, editor)
        }
      },
    }

    try {
      return new Editor(safeOptions)
    } catch (error) {
      this.handleError(error)
      return this.createFallbackEditor(options.element)
    }
  }

  /**
   * 处理错误
   * @param {Error} error - 错误对象
   * @param {Editor} editor - 编辑器实例
   */
  handleError(error, editor = null) {
    const errorInfo = {
      timestamp: new Date().toISOString(),
      message: error.message,
      stack: error.stack,
      editorState: editor ? this.getEditorState(editor) : null,
      userAgent: navigator.userAgent,
      url: window.location.href,
    }

    // 记录错误
    this.logError(errorInfo)

    // 根据错误类型采取不同处理策略
    this.handleErrorByType(error, editor)
  }

  /**
   * 根据错误类型处理
   * @param {Error} error - 错误对象
   * @param {Editor} editor - 编辑器实例
   */
  handleErrorByType(error, editor) {
    if (error.name === 'RangeError' && editor) {
      // 范围错误，尝试重置选择
      try {
        editor.commands.blur()
        editor.commands.focus()
      } catch (e) {
        console.warn('无法重置编辑器焦点:', e)
      }
    } else if (error.name === 'TypeError' && error.message.includes('Cannot read property')) {
      // 属性访问错误，可能是扩展问题
      console.warn('检测到扩展相关错误，建议检查扩展配置')
    } else if (error.name === 'NetworkError') {
      // 网络错误
      this.showNetworkErrorMessage()
    }
  }

  /**
   * 创建降级编辑器
   * @param {Element} element - 容器元素
   */
  createFallbackEditor(element) {
    console.warn('创建降级编辑器')
    
    // 创建简单的文本域作为降级方案
    const textarea = document.createElement('textarea')
    textarea.className = 'fallback-editor'
    textarea.placeholder = '编辑器加载失败，使用简化模式'
    textarea.style.cssText = `
      width: 100%;
      min-height: 200px;
      border: 1px solid #ccc;
      border-radius: 4px;
      padding: 12px;
      font-family: inherit;
      font-size: inherit;
      resize: vertical;
    `

    element.innerHTML = ''
    element.appendChild(textarea)

    return {
      element: textarea,
      getHTML: () => textarea.value.replace(/\n/g, '<br>'),
      getText: () => textarea.value,
      setContent: (content) => {
        textarea.value = content.replace(/<br>/g, '\n').replace(/<[^>]*>/g, '')
      },
      destroy: () => {
        if (textarea.parentNode) {
          textarea.parentNode.removeChild(textarea)
        }
      },
      isFallback: true,
    }
  }

  /**
   * 获取编辑器状态
   * @param {Editor} editor - 编辑器实例
   */
  getEditorState(editor) {
    try {
      return {
        content: editor.getHTML(),
        selection: editor.state.selection.toJSON(),
        extensionCount: editor.extensionManager.extensions.length,
        isEditable: editor.isEditable,
        isFocused: editor.isFocused,
      }
    } catch (e) {
      return { error: '无法获取编辑器状态' }
    }
  }

  /**
   * 记录错误
   * @param {Object} errorInfo - 错误信息
   */
  logError(errorInfo) {
    this.errorLog.push(errorInfo)

    // 限制日志大小
    if (this.errorLog.length > this.maxLogSize) {
      this.errorLog.shift()
    }

    // 发送错误报告（可选）
    this.reportError(errorInfo)
  }

  /**
   * 发送错误报告
   * @param {Object} errorInfo - 错误信息
   */
  reportError(errorInfo) {
    // 在生产环境中发送错误报告
    if (process.env.NODE_ENV === 'production') {
      fetch('/api/error-report', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(errorInfo),
      }).catch(e => {
        console.warn('发送错误报告失败:', e)
      })
    } else {
      console.error('编辑器错误:', errorInfo)
    }
  }

  /**
   * 显示网络错误消息
   */
  showNetworkErrorMessage() {
    // 显示用户友好的错误消息
    const message = document.createElement('div')
    message.className = 'editor-error-message'
    message.innerHTML = `
      <div style="
        background: #fee;
        border: 1px solid #fcc;
        border-radius: 4px;
        padding: 12px;
        margin: 8px 0;
        color: #c33;
      ">
        <strong>网络错误：</strong>无法连接到服务器，请检查网络连接。
      </div>
    `
    
    document.body.appendChild(message)
    
    // 3秒后自动移除
    setTimeout(() => {
      if (message.parentNode) {
        message.parentNode.removeChild(message)
      }
    }, 3000)
  }

  /**
   * 获取错误日志
   */
  getErrorLog() {
    return this.errorLog
  }

  /**
   * 清除错误日志
   */
  clearErrorLog() {
    this.errorLog = []
  }
}

// 使用示例
const errorHandler = new EditorErrorHandler()

const editor = errorHandler.createSafeEditor({
  element: document.querySelector('.editor'),
  extensions: [StarterKit],
  content: '<p>Hello World!</p>',
  onError: (error, editor) => {
    console.log('自定义错误处理:', error)
  },
})
```

## 常见问题解决方案

### 1. 性能问题

#### 问题：编辑器在大文档中响应缓慢

**解决方案：**

```javascript
// 1. 启用延迟渲染
const editor = new Editor({
  extensions: [
    StarterKit,
    // 添加延迟渲染扩展
    Extension.create({
      name: 'lazyRender',
      addProseMirrorPlugins() {
        return [
          new Plugin({
            props: {
              // 延迟DOM更新
              handleDOMEvents: {
                input: (view, event) => {
                  // 防抖处理输入事件
                  clearTimeout(this.inputTimer)
                  this.inputTimer = setTimeout(() => {
                    view.updateState(view.state)
                  }, 16) // 约60fps
                  return false
                },
              },
            },
          }),
        ]
      },
    }),
  ],
})

// 2. 限制历史记录深度
History.configure({
  depth: 50, // 默认是100
})

// 3. 禁用不必要的扩展
StarterKit.configure({
  dropcursor: false, // 如果不需要拖拽光标
  gapcursor: false,  // 如果不需要间隙光标
})
```

#### 问题：内存使用过高

**解决方案：**

```javascript
// 1. 定期清理历史记录
setInterval(() => {
  if (editor.storage.history) {
    // 清理旧的历史记录
    editor.commands.clearHistory()
  }
}, 300000) // 每5分钟清理一次

// 2. 限制文档大小
const MAX_CONTENT_SIZE = 1024 * 1024 // 1MB

const editor = new Editor({
  onUpdate: ({ editor }) => {
    const content = editor.getHTML()
    if (content.length > MAX_CONTENT_SIZE) {
      console.warn('文档过大，建议分割内容')
      // 可以实现自动分页或提示用户
    }
  },
})

// 3. 使用内容压缩
const compressContent = (content) => {
  // 移除多余的空白字符
  return content
    .replace(/\s+/g, ' ')
    .replace(/> </g, '><')
    .trim()
}
```

### 2. 兼容性问题

#### 问题：在旧版浏览器中无法正常工作

**解决方案：**

```javascript
// 1. 浏览器兼容性检测
const checkBrowserSupport = () => {
  const requiredFeatures = [
    'Promise',
    'Map',
    'Set',
    'Object.assign',
    'Array.from',
  ]

  const missingFeatures = requiredFeatures.filter(feature => {
    return !window[feature] && !Object[feature] && !Array[feature]
  })

  if (missingFeatures.length > 0) {
    console.warn('浏览器不支持以下特性:', missingFeatures)
    return false
  }

  // 检查 ProseMirror 支持
  if (!document.queryCommandSupported || !document.execCommand) {
    console.warn('浏览器不支持富文本编辑')
    return false
  }

  return true
}

// 2. 条件加载 Polyfill
const loadPolyfills = async () => {
  const polyfills = []

  if (!window.Promise) {
    polyfills.push(import('es6-promise/auto'))
  }

  if (!Array.from) {
    polyfills.push(import('core-js/features/array/from'))
  }

  if (!Object.assign) {
    polyfills.push(import('core-js/features/object/assign'))
  }

  await Promise.all(polyfills)
}

// 3. 创建兼容性编辑器
const createCompatibleEditor = async (options) => {
  if (!checkBrowserSupport()) {
    await loadPolyfills()
  }

  try {
    return new Editor(options)
  } catch (error) {
    console.warn('创建编辑器失败，使用降级方案')
    return createFallbackEditor(options.element)
  }
}
```

#### 问题：移动端触摸事件处理

**解决方案：**

```javascript
// 移动端优化扩展
const MobileOptimization = Extension.create({
  name: 'mobileOptimization',

  addProseMirrorPlugins() {
    return [
      new Plugin({
        props: {
          handleDOMEvents: {
            // 处理触摸事件
            touchstart: (view, event) => {
              // 防止双击缩放
              if (event.touches.length > 1) {
                event.preventDefault()
              }
            },
            touchmove: (view, event) => {
              // 处理滚动冲突
              if (event.touches.length === 1) {
                const touch = event.touches[0]
                const element = document.elementFromPoint(touch.clientX, touch.clientY)
                if (element && element.closest('.ProseMirror')) {
                  // 在编辑器内部，允许文本选择
                  return false
                }
              }
            },
            // 优化虚拟键盘
            focusin: (view, event) => {
              if (this.isMobile()) {
                // 滚动到编辑器位置
                setTimeout(() => {
                  view.dom.scrollIntoView({ behavior: 'smooth', block: 'center' })
                }, 300)
              }
            },
          },
        },
      }),
    ]
  },

  isMobile() {
    return /Android|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
  },
})

// 使用移动端优化
const editor = new Editor({
  extensions: [
    StarterKit,
    MobileOptimization,
  ],
})
```

### 3. 内容处理问题

#### 问题：粘贴内容格式混乱

**解决方案：**

```javascript
// 自定义粘贴处理
const CleanPaste = Extension.create({
  name: 'cleanPaste',

  addProseMirrorPlugins() {
    return [
      new Plugin({
        props: {
          handlePaste: (view, event, slice) => {
            const { clipboardData } = event
            
            // 获取纯文本和HTML内容
            const text = clipboardData.getData('text/plain')
            const html = clipboardData.getData('text/html')

            // 清理HTML内容
            if (html) {
              const cleanedHtml = this.cleanHtml(html)
              const parser = DOMParser.fromSchema(view.state.schema)
              const doc = parser.parse(cleanedHtml)
              
              view.dispatch(
                view.state.tr.replaceSelectionWith(doc, false)
              )
              return true
            }

            // 处理纯文本
            if (text) {
              const lines = text.split('\n')
              const content = lines.map(line => `<p>${line || '<br>'}</p>`).join('')
              
              view.dispatch(
                view.state.tr.insertText(content)
              )
              return true
            }

            return false
          },
        },
      }),
    ]
  },

  cleanHtml(html) {
    // 创建临时DOM元素
    const temp = document.createElement('div')
    temp.innerHTML = html

    // 移除不需要的标签和属性
    const allowedTags = ['p', 'br', 'strong', 'em', 'u', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'ul', 'ol', 'li']
    const allowedAttributes = ['href', 'src', 'alt']

    this.cleanElement(temp, allowedTags, allowedAttributes)

    return temp.innerHTML
  },

  cleanElement(element, allowedTags, allowedAttributes) {
    const children = Array.from(element.children)
    
    children.forEach(child => {
      // 检查标签是否允许
      if (!allowedTags.includes(child.tagName.toLowerCase())) {
        // 不允许的标签，保留内容但移除标签
        const parent = child.parentNode
        while (child.firstChild) {
          parent.insertBefore(child.firstChild, child)
        }
        parent.removeChild(child)
        return
      }

      // 清理属性
      const attributes = Array.from(child.attributes)
      attributes.forEach(attr => {
        if (!allowedAttributes.includes(attr.name)) {
          child.removeAttribute(attr.name)
        }
      })

      // 递归清理子元素
      this.cleanElement(child, allowedTags, allowedAttributes)
    })
  },
})
```

#### 问题：图片上传和处理

**解决方案：**

```javascript
// 图片上传处理扩展
const ImageUpload = Extension.create({
  name: 'imageUpload',

  addOptions() {
    return {
      uploadUrl: '/api/upload',
      maxSize: 5 * 1024 * 1024, // 5MB
      allowedTypes: ['image/jpeg', 'image/png', 'image/gif', 'image/webp'],
    }
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        props: {
          handleDOMEvents: {
            drop: (view, event) => {
              const files = Array.from(event.dataTransfer.files)
              const imageFiles = files.filter(file => 
                this.options.allowedTypes.includes(file.type)
              )

              if (imageFiles.length > 0) {
                event.preventDefault()
                this.handleImageUpload(view, imageFiles, event)
                return true
              }
            },
            paste: (view, event) => {
              const files = Array.from(event.clipboardData.files)
              const imageFiles = files.filter(file => 
                this.options.allowedTypes.includes(file.type)
              )

              if (imageFiles.length > 0) {
                event.preventDefault()
                this.handleImageUpload(view, imageFiles, event)
                return true
              }
            },
          },
        },
      }),
    ]
  },

  async handleImageUpload(view, files, event) {
    for (const file of files) {
      // 检查文件大小
      if (file.size > this.options.maxSize) {
        this.showError(`图片 ${file.name} 超过大小限制 ${this.options.maxSize / 1024 / 1024}MB`)
        continue
      }

      try {
        // 显示上传进度
        const placeholder = this.insertPlaceholder(view, file.name)
        
        // 上传图片
        const imageUrl = await this.uploadImage(file, (progress) => {
          this.updatePlaceholder(placeholder, progress)
        })

        // 替换占位符为实际图片
        this.replacePlaceholder(view, placeholder, imageUrl)
      } catch (error) {
        this.showError(`上传图片 ${file.name} 失败: ${error.message}`)
        this.removePlaceholder(view, placeholder)
      }
    }
  },

  insertPlaceholder(view, fileName) {
    const placeholder = document.createElement('div')
    placeholder.className = 'image-upload-placeholder'
    placeholder.innerHTML = `
      <div class="upload-progress">
        <div class="upload-info">
          <span class="file-name">${fileName}</span>
          <span class="progress-text">上传中... 0%</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" style="width: 0%"></div>
        </div>
      </div>
    `

    const pos = view.state.selection.from
    view.dispatch(
      view.state.tr.insert(pos, placeholder)
    )

    return placeholder
  },

  updatePlaceholder(placeholder, progress) {
    const progressText = placeholder.querySelector('.progress-text')
    const progressFill = placeholder.querySelector('.progress-fill')
    
    if (progressText) {
      progressText.textContent = `上传中... ${Math.round(progress)}%`
    }
    if (progressFill) {
      progressFill.style.width = `${progress}%`
    }
  },

  replacePlaceholder(view, placeholder, imageUrl) {
    // 找到占位符在文档中的位置
    const pos = this.findPlaceholderPosition(view, placeholder)
    if (pos !== -1) {
      view.dispatch(
        view.state.tr
          .delete(pos, pos + 1)
          .insert(pos, view.state.schema.nodes.image.create({ src: imageUrl }))
      )
    }
  },

  removePlaceholder(view, placeholder) {
    const pos = this.findPlaceholderPosition(view, placeholder)
    if (pos !== -1) {
      view.dispatch(view.state.tr.delete(pos, pos + 1))
    }
  },

  findPlaceholderPosition(view, placeholder) {
    // 实现查找占位符位置的逻辑
    // 这里简化处理，实际应用中需要更精确的位置查找
    return view.state.selection.from
  },

  async uploadImage(file, onProgress) {
    const formData = new FormData()
    formData.append('image', file)

    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest()

      xhr.upload.addEventListener('progress', (event) => {
        if (event.lengthComputable) {
          const progress = (event.loaded / event.total) * 100
          onProgress(progress)
        }
      })

      xhr.addEventListener('load', () => {
        if (xhr.status === 200) {
          const response = JSON.parse(xhr.responseText)
          resolve(response.url)
        } else {
          reject(new Error(`上传失败: ${xhr.statusText}`))
        }
      })

      xhr.addEventListener('error', () => {
        reject(new Error('网络错误'))
      })

      xhr.open('POST', this.options.uploadUrl)
      xhr.send(formData)
    })
  },

  showError(message) {
    // 显示错误消息
    console.error(message)
    // 可以集成到应用的通知系统
  },
})
```

### 4. 协作问题

#### 问题：多用户协作冲突

**解决方案：**

```javascript
// 协作冲突解决
const CollaborationConflictResolver = Extension.create({
  name: 'collaborationConflictResolver',

  addOptions() {
    return {
      conflictResolutionStrategy: 'merge', // 'merge' | 'overwrite' | 'manual'
      showConflictUI: true,
    }
  },

  addStorage() {
    return {
      conflicts: [],
      isResolvingConflict: false,
    }
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        state: {
          init: () => ({}),
          apply: (tr, value, oldState, newState) => {
            // 检测冲突
            if (tr.getMeta('collaboration-conflict')) {
              this.handleConflict(tr.getMeta('collaboration-conflict'))
            }
            return value
          },
        },
      }),
    ]
  },

  handleConflict(conflictData) {
    const { localChanges, remoteChanges, conflictPosition } = conflictData

    switch (this.options.conflictResolutionStrategy) {
      case 'merge':
        this.mergeConflicts(localChanges, remoteChanges, conflictPosition)
        break
      case 'overwrite':
        this.overwriteWithRemote(remoteChanges, conflictPosition)
        break
      case 'manual':
        this.showConflictResolutionUI(localChanges, remoteChanges, conflictPosition)
        break
    }
  },

  mergeConflicts(localChanges, remoteChanges, position) {
    // 实现智能合并逻辑
    const mergedContent = this.intelligentMerge(localChanges, remoteChanges)
    
    this.editor.commands.insertContentAt(position, mergedContent)
    this.notifyConflictResolved('merge')
  },

  intelligentMerge(localChanges, remoteChanges) {
    // 简化的合并逻辑
    // 实际应用中需要更复杂的算法
    if (localChanges.type === 'text' && remoteChanges.type === 'text') {
      // 文本合并
      return `${localChanges.content} ${remoteChanges.content}`
    }
    
    // 默认保留本地更改
    return localChanges.content
  },

  showConflictResolutionUI(localChanges, remoteChanges, position) {
    if (!this.options.showConflictUI) return

    const conflictUI = document.createElement('div')
    conflictUI.className = 'conflict-resolution-ui'
    conflictUI.innerHTML = `
      <div class="conflict-header">
        <h3>检测到编辑冲突</h3>
        <p>多个用户同时编辑了相同内容，请选择如何处理：</p>
      </div>
      <div class="conflict-options">
        <div class="conflict-option">
          <h4>您的更改：</h4>
          <div class="conflict-content local">${localChanges.content}</div>
          <button class="btn-keep-local">保留我的更改</button>
        </div>
        <div class="conflict-option">
          <h4>其他用户的更改：</h4>
          <div class="conflict-content remote">${remoteChanges.content}</div>
          <button class="btn-keep-remote">保留其他用户的更改</button>
        </div>
        <div class="conflict-option">
          <h4>合并更改：</h4>
          <textarea class="conflict-merge-input">${this.intelligentMerge(localChanges, remoteChanges)}</textarea>
          <button class="btn-merge">使用合并结果</button>
        </div>
      </div>
    `

    // 添加事件监听器
    conflictUI.querySelector('.btn-keep-local').addEventListener('click', () => {
      this.resolveConflict('local', localChanges.content, position)
      this.removeConflictUI(conflictUI)
    })

    conflictUI.querySelector('.btn-keep-remote').addEventListener('click', () => {
      this.resolveConflict('remote', remoteChanges.content, position)
      this.removeConflictUI(conflictUI)
    })

    conflictUI.querySelector('.btn-merge').addEventListener('click', () => {
      const mergedContent = conflictUI.querySelector('.conflict-merge-input').value
      this.resolveConflict('merge', mergedContent, position)
      this.removeConflictUI(conflictUI)
    })

    document.body.appendChild(conflictUI)
  },

  resolveConflict(strategy, content, position) {
    this.editor.commands.insertContentAt(position, content)
    this.notifyConflictResolved(strategy)
  },

  removeConflictUI(ui) {
    if (ui.parentNode) {
      ui.parentNode.removeChild(ui)
    }
  },

  notifyConflictResolved(strategy) {
    console.log(`冲突已解决，策略: ${strategy}`)
    // 可以发送通知给其他协作者
  },
})
```

## 调试和开发工具

### 1. 开发者工具扩展

```javascript
// 开发者调试工具
const DevTools = Extension.create({
  name: 'devTools',

  addOptions() {
    return {
      enabled: process.env.NODE_ENV === 'development',
      showStateInConsole: true,
      showPerformanceMetrics: true,
    }
  },

  addStorage() {
    return {
      metrics: {
        renderTime: [],
        updateCount: 0,
        lastUpdate: null,
      },
    }
  },

  onCreate() {
    if (!this.options.enabled) return

    // 添加全局调试方法
    window.tiptapDebug = {
      editor: this.editor,
      getState: () => this.editor.state,
      getHTML: () => this.editor.getHTML(),
      getJSON: () => this.editor.getJSON(),
      getMetrics: () => this.storage.metrics,
      logExtensions: () => {
        console.table(
          this.editor.extensionManager.extensions.map(ext => ({
            name: ext.name,
            type: ext.type,
            priority: ext.priority,
          }))
        )
      },
      logCommands: () => {
        console.log('可用命令:', Object.keys(this.editor.commands))
      },
    }

    console.log('Tiptap 调试工具已启用，使用 window.tiptapDebug 访问')
  },

  onUpdate() {
    if (!this.options.enabled) return

    const startTime = performance.now()
    
    // 记录更新指标
    this.storage.metrics.updateCount++
    this.storage.metrics.lastUpdate = new Date().toISOString()

    if (this.options.showStateInConsole) {
      console.group('编辑器状态更新')
      console.log('更新次数:', this.storage.metrics.updateCount)
      console.log('文档大小:', this.editor.state.doc.nodeSize)
      console.log('选择位置:', this.editor.state.selection.from, '-', this.editor.state.selection.to)
      console.groupEnd()
    }

    // 记录渲染时间
    requestAnimationFrame(() => {
      const renderTime = performance.now() - startTime
      this.storage.metrics.renderTime.push(renderTime)
      
      // 保留最近100次记录
      if (this.storage.metrics.renderTime.length > 100) {
        this.storage.metrics.renderTime.shift()
      }

      if (this.options.showPerformanceMetrics && renderTime > 16) {
        console.warn(`渲染时间过长: ${renderTime.toFixed(2)}ms`)
      }
    })
  },

  addCommands() {
    return {
      debugState: () => () => {
        console.log('当前状态:', this.editor.state)
        return true
      },
      debugSelection: () => () => {
        console.log('当前选择:', this.editor.state.selection)
        return true
      },
      debugPerformance: () => () => {
        const metrics = this.storage.metrics
        const avgRenderTime = metrics.renderTime.reduce((a, b) => a + b, 0) / metrics.renderTime.length
        
        console.table({
          '更新次数': metrics.updateCount,
          '平均渲染时间': `${avgRenderTime.toFixed(2)}ms`,
          '最大渲染时间': `${Math.max(...metrics.renderTime).toFixed(2)}ms`,
          '最后更新': metrics.lastUpdate,
        })
        return true
      },
    }
  },
})
```

### 2. 单元测试工具

```javascript
// 测试工具类
class TiptapTestUtils {
  constructor() {
    this.editors = new Map()
  }

  /**
   * 创建测试编辑器
   * @param {Object} options - 编辑器选项
   * @returns {Editor} 编辑器实例
   */
  createTestEditor(options = {}) {
    const defaultOptions = {
      extensions: [Document, Paragraph, Text],
      content: '',
    }

    const editor = new Editor({ ...defaultOptions, ...options })
    const id = Math.random().toString(36).substr(2, 9)
    this.editors.set(id, editor)

    return { editor, id }
  }

  /**
   * 销毁测试编辑器
   * @param {string} id - 编辑器ID
   */
  destroyTestEditor(id) {
    const editor = this.editors.get(id)
    if (editor) {
      editor.destroy()
      this.editors.delete(id)
    }
  }

  /**
   * 销毁所有测试编辑器
   */
  destroyAllTestEditors() {
    this.editors.forEach((editor, id) => {
      this.destroyTestEditor(id)
    })
  }

  /**
   * 模拟用户输入
   * @param {Editor} editor - 编辑器实例
   * @param {string} text - 输入文本
   */
  typeText(editor, text) {
    editor.commands.insertContent(text)
  }

  /**
   * 模拟键盘事件
   * @param {Editor} editor - 编辑器实例
   * @param {string} key - 按键
   * @param {Object} modifiers - 修饰键
   */
  pressKey(editor, key, modifiers = {}) {
    const event = new KeyboardEvent('keydown', {
      key,
      ctrlKey: modifiers.ctrl || false,
      shiftKey: modifiers.shift || false,
      altKey: modifiers.alt || false,
      metaKey: modifiers.meta || false,
    })

    editor.view.dom.dispatchEvent(event)
  }

  /**
   * 设置选择范围
   * @param {Editor} editor - 编辑器实例
   * @param {number} from - 开始位置
   * @param {number} to - 结束位置
   */
  setSelection(editor, from, to = from) {
    editor.commands.setTextSelection({ from, to })
  }

  /**
   * 获取文档内容
   * @param {Editor} editor - 编辑器实例
   * @returns {Object} 文档内容
   */
  getDocContent(editor) {
    return {
      html: editor.getHTML(),
      json: editor.getJSON(),
      text: editor.getText(),
    }
  }

  /**
   * 断言编辑器状态
   * @param {Editor} editor - 编辑器实例
   * @param {Object} expected - 期望状态
   */
  assertEditorState(editor, expected) {
    const actual = {
      html: editor.getHTML(),
      selection: {
        from: editor.state.selection.from,
        to: editor.state.selection.to,
      },
      canUndo: editor.can().undo(),
      canRedo: editor.can().redo(),
    }

    Object.keys(expected).forEach(key => {
      if (key === 'selection') {
        expect(actual.selection.from).toBe(expected.selection.from)
        expect(actual.selection.to).toBe(expected.selection.to)
      } else {
        expect(actual[key]).toBe(expected[key])
      }
    })
  }
}

// 使用示例
const testUtils = new TiptapTestUtils()

describe('Tiptap Editor', () => {
  let editor, editorId

  beforeEach(() => {
    const result = testUtils.createTestEditor({
      extensions: [StarterKit],
      content: '<p>Hello World</p>',
    })
    editor = result.editor
    editorId = result.id
  })

  afterEach(() => {
    testUtils.destroyTestEditor(editorId)
  })

  it('should handle text input', () => {
    testUtils.setSelection(editor, 11) // 在 "World" 后面
    testUtils.typeText(editor, '!')
    
    testUtils.assertEditorState(editor, {
      html: '<p>Hello World!</p>',
      selection: { from: 12, to: 12 },
    })
  })

  it('should handle bold formatting', () => {
    testUtils.setSelection(editor, 6, 11) // 选择 "World"
    editor.commands.toggleBold()
    
    testUtils.assertEditorState(editor, {
      html: '<p>Hello <strong>World</strong></p>',
    })
  })

  it('should handle undo/redo', () => {
    testUtils.typeText(editor, ' Test')
    editor.commands.undo()
    
    testUtils.assertEditorState(editor, {
      html: '<p>Hello World</p>',
      canRedo: true,
    })
  })
})
```

## 总结

本文档涵盖了 Tiptap 编辑器开发中的最佳实践和常见问题解决方案，包括：

### 最佳实践要点

1. **性能优化**：延迟初始化、内容分片、防抖处理
2. **内存管理**：正确销毁编辑器、监控内存使用
3. **错误处理**：全局错误捕获、降级方案、用户友好提示
4. **兼容性**：浏览器检测、Polyfill 加载、移动端优化

### 常见问题解决

1. **性能问题**：大文档处理、内存优化
2. **兼容性问题**：旧浏览器支持、移动端适配
3. **内容处理**：粘贴清理、图片上传
4. **协作问题**：冲突解决、状态同步

### 开发工具

1. **调试工具**：状态监控、性能分析
2. **测试工具**：单元测试、集成测试

通过遵循这些最佳实践和解决方案，可以构建出稳定、高性能的 Tiptap 编辑器应用。

---

*至此，Tiptap 编辑器使用指南文档已完成。这些文档涵盖了从基础使用到高级功能的完整开发指南，帮助开发者快速上手并构建高质量的富文本编辑器应用。*