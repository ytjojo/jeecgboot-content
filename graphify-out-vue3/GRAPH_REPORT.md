# Graph Report - /Users/yangtengjiao/Documents/j2ee/JeecgBoot_sass/jeecgboot-vue3  (2026-05-18)

## Corpus Check
- Large corpus: 1567 files · ~806,352 words. Semantic extraction will be expensive (many Claude tokens). Consider running on a subfolder, or use --no-semantic to run AST-only.

## Summary
- 4738 nodes · 8479 edges · 62 communities detected
- Extraction: 88% EXTRACTED · 12% INFERRED · 0% AMBIGUOUS · INFERRED: 1020 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_User Module|User Module]]
- [[_COMMUNITY_API Layer|API Layer]]
- [[_COMMUNITY_setup()|setup()]]
- [[_COMMUNITY_createAxios()|createAxios()]]
- [[_COMMUNITY_TableList Components|Table/List Components]]
- [[_COMMUNITY_TableList Components|Table/List Components]]
- [[_COMMUNITY_Form Components|Form Components]]
- [[_COMMUNITY_Dictionary|Dictionary]]
- [[_COMMUNITY_Vue Components|Vue Components]]
- [[_COMMUNITY_Background|Background]]
- [[_COMMUNITY_User Module|User Module]]
- [[_COMMUNITY_TableList Components|Table/List Components]]
- [[_COMMUNITY_DemoExamples|Demo/Examples]]
- [[_COMMUNITY_TableList Components|Table/List Components]]
- [[_COMMUNITY_Memory|Memory]]
- [[_COMMUNITY_Configuration|Configuration]]
- [[_COMMUNITY_AuthLogin|Auth/Login]]
- [[_COMMUNITY_handleSubmit()|handleSubmit()]]
- [[_COMMUNITY_if()|if()]]
- [[_COMMUNITY_Form Components|Form Components]]
- [[_COMMUNITY_Vue Components|Vue Components]]
- [[_COMMUNITY_Form Components|Form Components]]
- [[_COMMUNITY_setup()|setup()]]
- [[_COMMUNITY_getRandom()|getRandom()]]
- [[_COMMUNITY_API Layer|API Layer]]
- [[_COMMUNITY_Configuration|Configuration]]
- [[_COMMUNITY_setup()|setup()]]
- [[_COMMUNITY_batchHandleDelete()|batchHandleDelete()]]
- [[_COMMUNITY_TableList Components|Table/List Components]]
- [[_COMMUNITY_DayUI.vue|DayUI.vue]]
- [[_COMMUNITY_AbstractRichText|AbstractRichText]]
- [[_COMMUNITY_Form Components|Form Components]]
- [[_COMMUNITY_TableList Components|Table/List Components]]
- [[_COMMUNITY_index.ts|index.ts]]
- [[_COMMUNITY_Vue Components|Vue Components]]
- [[_COMMUNITY_main()|main()]]
- [[_COMMUNITY_Utilities|Utilities]]
- [[_COMMUNITY_Area.ts|Area.ts]]
- [[_COMMUNITY_Form Components|Form Components]]
- [[_COMMUNITY_Vue Components|Vue Components]]
- [[_COMMUNITY_ComposablesHooks|Composables/Hooks]]
- [[_COMMUNITY_User Module|User Module]]
- [[_COMMUNITY_Type Definitions|Type Definitions]]
- [[_COMMUNITY_ExpandTransition.ts|ExpandTransition.ts]]
- [[_COMMUNITY_CheckboxControl|CheckboxControl]]
- [[_COMMUNITY_Type Definitions|Type Definitions]]
- [[_COMMUNITY_Form Components|Form Components]]
- [[_COMMUNITY_API Layer|API Layer]]
- [[_COMMUNITY_getInfo()|getInfo()]]
- [[_COMMUNITY_bem.ts|bem.ts]]
- [[_COMMUNITY_browser.js|browser.js]]
- [[_COMMUNITY_ControlBorder|ControlBorder]]
- [[_COMMUNITY_WorkerManager|WorkerManager]]
- [[_COMMUNITY_isButton()|isButton()]]
- [[_COMMUNITY_Styles|Styles]]
- [[_COMMUNITY_TableList Components|Table/List Components]]
- [[_COMMUNITY_User Module|User Module]]
- [[_COMMUNITY_VideoBlock|VideoBlock]]
- [[_COMMUNITY_ComposablesHooks|Composables/Hooks]]
- [[_COMMUNITY_Small 92|Small 92]]
- [[_COMMUNITY_User Module|User Module]]
- [[_COMMUNITY_Configuration|Configuration]]

## God Nodes (most connected - your core abstractions)
1. `useMessage()` - 155 edges
2. `Draw` - 126 edges
3. `CommandAdapt` - 120 edges
4. `unref()` - 108 edges
5. `useI18n()` - 81 edges
6. `useDesign()` - 78 edges
7. `isFunction()` - 60 edges
8. `propTypes` - 56 edges
9. `useAttrs()` - 50 edges
10. `Control` - 41 edges

## Surprising Connections (you probably didn't know these)
- `onIframeLoad()` --calls--> `buildUUID()`  [INFERRED]
  src/views/system/notice/DetailModal.vue → src/utils/uuid.ts
- `getIcon()` --calls--> `isFunction()`  [INFERRED]
  src/components/Tree_backup/src/Tree.vue → src/utils/is.ts
- `handleRightClick()` --calls--> `isFunction()`  [INFERRED]
  src/components/Tree_backup/src/Tree.vue → src/utils/is.ts
- `getImage()` --calls--> `getFileAccessHttpUrl()`  [INFERRED]
  src/views/super/airag/aiprompts/components/AiPromptSettingModal.vue → src/utils/common/compUtils.ts
- `translateTitle()` --calls--> `useI18n()`  [INFERRED]
  src/utils/common/compUtils.ts → src/hooks/web/useI18n.ts

## Communities

### Community 0 - "User Module"
Cohesion: 0.01
Nodes (129): replaceUserInfoByExpression(), align(), asciiMap(), backspace(), CanvasEvent, CheckboxParticle, cloneProperty(), Command (+121 more)

### Community 1 - "API Layer"
Cohesion: 0.01
Nodes (152): formatRequestDate(), joinTimestamp(), createValue(), getCustomValue(), getValue(), neverNull(), setup(), setValue() (+144 more)

### Community 2 - "setup()"
Cohesion: 0.01
Nodes (121): setup(), saveTools(), setup(), setup(), setup(), setup(), handleDel(), reload() (+113 more)

### Community 3 - "createAxios()"
Cohesion: 0.01
Nodes (119): createAxios(), init(), setTheme(), uploadFile(), checkChildrenHidden(), createPageContext(), usePageContext(), setup() (+111 more)

### Community 4 - "Table/List Components"
Cohesion: 0.01
Nodes (61): saveAndSync(), handleGenerate(), handleReset(), handleSubmit(), extractVariables(), getImage(), handleContentChange(), handleOk() (+53 more)

### Community 5 - "Table/List Components"
Cohesion: 0.01
Nodes (73): knowledgeDeleteAllDoc(), doDeleteAllDoc(), batchDelete(), batchDelete(), getTenantId(), calculateFileSize(), findTree(), freezeDeep() (+65 more)

### Community 6 - "Form Components"
Cohesion: 0.01
Nodes (105): queryAppVersion(), handleSubmit(), initFormData(), uploadBack(), updateTableDataRecord(), handleSubmit(), getExpandKeysByPid(), getSameLevelExpandKeysByPid() (+97 more)

### Community 7 - "Dictionary"
Cohesion: 0.02
Nodes (70): setup(), setup(), downloadFile(), getDictItems(), getFileblob(), getRoleList(), loadDictItem(), loadTreeData() (+62 more)

### Community 8 - "Vue Components"
Cohesion: 0.02
Nodes (86): checkStatus(), getUserInfoByExpression(), registerGlobComp(), IFrame(), ExceptionPage(), hookNavigate(), hookWindowOpen(), setupElectron() (+78 more)

### Community 9 - "Background"
Cohesion: 0.02
Nodes (21): Background, BaseBlock, BlockParticle, ContextMenu, convertStringToBase64(), Cursor, DatePicker, debounce() (+13 more)

### Community 10 - "User Module"
Cohesion: 0.02
Nodes (72): handleConfirm(), getAuthCache(), getLoginBackInfo(), getToken(), setAuthCache(), getCaptchaCode(), useRuleFormItem(), setup() (+64 more)

### Community 11 - "Table/List Components"
Cohesion: 0.02
Nodes (57): setup(), getGloablEmojiIndex(), useCommentWithFile(), useEmojiHtml(), loadCategoryData(), filterObj(), getElectronFileUrl(), getFileAccessHttpUrl() (+49 more)

### Community 12 - "Demo/Examples"
Cohesion: 0.03
Nodes (59): createLocalStorage(), createOptions(), createSessionStorage(), createStorage(), Persistent, storageChange(), createStorage(), setup() (+51 more)

### Community 13 - "Table/List Components"
Cohesion: 0.04
Nodes (10): deleteSurroundElementList(), Footer, Header, LineNumber, PageNumber, Placeholder, SubscriptParticle, SuperscriptParticle (+2 more)

### Community 14 - "Memory"
Cohesion: 0.05
Nodes (44): Memory, setTimeout(), syncSelectedRows(), m(), s(), doDelete(), handleTableSet(), onJVxeRemove() (+36 more)

### Community 15 - "Configuration"
Cohesion: 0.02
Nodes (28): getApplicationData(), handleSave(), handleSettingsOk(), initChartData(), initChat(), parseJeecgTag(), getHeaders(), add() (+20 more)

### Community 16 - "Auth/Login"
Cohesion: 0.04
Nodes (22): getLoginfo(), getVisitInfo(), setup(), setup(), setup(), setup(), setup(), setup() (+14 more)

### Community 17 - "handleSubmit()"
Cohesion: 0.04
Nodes (21): handleSubmit(), handleSubmit(), doSave(), getNodeAllKey(), getTree(), handleSubmit(), onCheck(), onTreeNodeSelect() (+13 more)

### Community 18 - "if()"
Cohesion: 0.04
Nodes (23): setFieldsValue(), onCategoryChange(), handleSubmit(), itemColorClick(), saveOrUpdateDictItem(), updateFormValue(), handleDelete(), handleRelease() (+15 more)

### Community 19 - "Form Components"
Cohesion: 0.04
Nodes (15): handleOk(), treeSelect(), handleOk(), autoExpandParentNode(), loadDepartTreeData(), onDeleteBatch(), reloadTree(), deleteBatchDepart() (+7 more)

### Community 20 - "Vue Components"
Cohesion: 0.06
Nodes (38): handleTable1PageChange(), handleTable1SelectRowChange(), handleTable2PageChange(), loadSubData(), loadTable1Data(), loadTable2Data(), created(), handleTable1PageChange() (+30 more)

### Community 21 - "Form Components"
Cohesion: 0.06
Nodes (44): dateFormat(), queryDiskInfo(), loadRedisInfo(), assignInput(), calTriggerListInner(), convertWeekToQuartz(), emitValue(), formatValue() (+36 more)

### Community 22 - "setup()"
Cohesion: 0.06
Nodes (20): setup(), setup(), setup(), setupGlobDirectives(), setupLoadingDirective(), isAuth(), mounted(), setupPermissionDirective() (+12 more)

### Community 23 - "getRandom()"
Cohesion: 0.09
Nodes (16): getRandom(), handleSubmit(), uploadApi(), uploadImg(), initUserDetail(), editRealName(), getUserDetail(), handleChange() (+8 more)

### Community 24 - "API Layer"
Cohesion: 0.09
Nodes (9): batchDelete(), downloadTpl(), generateResume(), generateWord(), extractTplFields(), handleDesignDownload(), handleGenOk(), handleGenResumeOk() (+1 more)

### Community 25 - "Configuration"
Cohesion: 0.13
Nodes (3): VAxios, AxiosCanceler, getPendingUrl()

### Community 26 - "setup()"
Cohesion: 0.14
Nodes (13): dataURLtoBlob(), urlToBase64(), downloadByBase64(), downloadByData(), downloadByOnlineUrl(), downloadByUrl(), beforeMenuClickFn(), getErrorCorrectionLevel() (+5 more)

### Community 27 - "batchHandleDelete()"
Cohesion: 0.11
Nodes (13): batchHandleDelete(), handleDelete(), handlerExecute(), handlerPause(), handlerResume(), batchDeleteQuartz(), deleteQuartz(), executeImmediately() (+5 more)

### Community 28 - "Table/List Components"
Cohesion: 0.11
Nodes (7): batchDeleteCheckRule(), deleteCheckRule(), saveCheckRule(), updateCheckRule(), saveOrUpdateFormData(), batchHandleDelete(), handleDelete()

### Community 29 - "DayUI.vue"
Cohesion: 0.25
Nodes (10): setup(), setup(), setup(), setup(), setup(), useTabEmits(), useTabProps(), useTabSetup() (+2 more)

### Community 30 - "AbstractRichText"
Cohesion: 0.14
Nodes (4): AbstractRichText, Highlight, Strikeout, Underline

### Community 31 - "Form Components"
Cohesion: 0.19
Nodes (6): getRequestToken(), pagination(), resultError(), resultPageSuccess(), resultSuccess(), createFakeUserList()

### Community 32 - "Table/List Components"
Cohesion: 0.14
Nodes (7): batchDeleteFillRule(), deleteFillRule(), saveFillRule(), updateFillRule(), handleSubmit(), batchHandleDelete(), handleDelete()

### Community 33 - "index.ts"
Cohesion: 0.14
Nodes (6): getIcon(), handleClickNode(), handleRightClick(), handleSearch(), setCheckedKeys(), setExpandedKeys()

### Community 34 - "Vue Components"
Cohesion: 0.23
Nodes (12): created(), handleTable1PageChange(), handleTable1SelectRowChange(), handleTable2PageChange(), handleTable2SelectRowChange(), handleTable3PageChange(), handleTableSelectRowChange(), loadTable1Data() (+4 more)

### Community 36 - "main()"
Cohesion: 0.31
Nodes (7): main(), createTray(), useTray(), createBrowserWindow(), createIndexWindow(), createMainWindow(), getBrowserWindowOptions()

### Community 37 - "Utilities"
Cohesion: 0.36
Nodes (1): signMd5Utils

### Community 38 - "Area.ts"
Cohesion: 0.28
Nodes (1): Area

### Community 39 - "Form Components"
Cohesion: 0.31
Nodes (4): validateFormModelAndTables(), validateTables(), setup(), useJvxeMethod()

### Community 41 - "Vue Components"
Cohesion: 0.25
Nodes (2): if(), createImgPreview()

### Community 43 - "Composables/Hooks"
Cohesion: 0.36
Nodes (4): setup(), setup(), setup(), useScript()

### Community 47 - "User Module"
Cohesion: 0.29
Nodes (2): Result, UserService

### Community 48 - "Type Definitions"
Cohesion: 0.29
Nodes (1): buildProps()

### Community 49 - "ExpandTransition.ts"
Cohesion: 0.33
Nodes (2): afterLeave(), resetStyles()

### Community 51 - "CheckboxControl"
Cohesion: 0.29
Nodes (1): CheckboxControl

### Community 52 - "Type Definitions"
Cohesion: 0.43
Nodes (4): buildMultiSeriesItem(), resolveName(), resolveNumber(), resolveSeriesName()

### Community 54 - "Form Components"
Cohesion: 0.4
Nodes (1): SelectionObserver

### Community 55 - "API Layer"
Cohesion: 0.4
Nodes (2): sessionTimeoutApi(), tokenExpiredApi()

### Community 56 - "getInfo()"
Cohesion: 0.47
Nodes (3): getKeysSize(), getMemoryInfo(), getRedisInfo()

### Community 58 - "bem.ts"
Cohesion: 0.6
Nodes (3): buildBEM(), createBEM(), createNamespace()

### Community 59 - "browser.js"
Cohesion: 0.8
Nodes (4): getIEVersion(), isEdge(), isIE(), isIE11()

### Community 61 - "ControlBorder"
Cohesion: 0.6
Nodes (1): ControlBorder

### Community 62 - "WorkerManager"
Cohesion: 0.4
Nodes (1): WorkerManager

### Community 65 - "isButton()"
Cohesion: 0.6
Nodes (3): isButton(), isDir(), isMenu()

### Community 67 - "Styles"
Cohesion: 0.7
Nodes (4): createAiChat(), getIframeSrc(), getPositionStyles(), isMobileDevice()

### Community 71 - "Table/List Components"
Cohesion: 0.83
Nodes (3): loadData(), loadRootData(), onExpand()

### Community 76 - "User Module"
Cohesion: 0.67
Nodes (1): UserService

### Community 80 - "VideoBlock"
Cohesion: 0.67
Nodes (1): VideoBlock

### Community 81 - "Composables/Hooks"
Cohesion: 0.67
Nodes (1): Plugin

### Community 92 - "Small 92"
Cohesion: 1.0
Nodes (1): FileController

### Community 93 - "User Module"
Cohesion: 1.0
Nodes (1): UserController

### Community 106 - "Configuration"
Cohesion: 1.0
Nodes (1): PaginationConfig

## Knowledge Gaps
- **4 isolated node(s):** `FileController`, `UserController`, `PaginationConfig`, `Override`
  These have ≤1 connection - possible missing edges or undocumented components.
- **Thin community `Utilities`** (9 nodes): `signMd5Utils`, `.getSign()`, `.getTimestamp()`, `.mergeObject()`, `.myIsNaN()`, `.parseQueryString()`, `.sortAsc()`, `.urlEncode()`, `signMd5Utils.js`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Area.ts`** (9 nodes): `Area.ts`, `Area`, `.constructor()`, `.getAreaBycode()`, `.getCode()`, `.getPcode()`, `.getRealCode()`, `.getText()`, `.pca()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Vue Components`** (8 nodes): `if()`, `JVxeImageCell.vue`, `functional.ts`, `Functional.vue`, `createImgPreview()`, `if()`, `init()`, `scaleFunc()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `User Module`** (7 nodes): `Result`, `.success()`, `UserService`, `.getUserInfoById()`, `.login()`, `UserService.ts`, `utils.ts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Type Definitions`** (7 nodes): `tree.ts`, `props.ts`, `buildProp()`, `buildProps()`, `definePropType()`, `keyOf()`, `mutable()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ExpandTransition.ts`** (7 nodes): `ExpandTransition.ts`, `afterLeave()`, `beforeEnter()`, `enter()`, `leave()`, `resetStyles()`, `upperFirst()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `CheckboxControl`** (7 nodes): `CheckboxControl`, `.constructor()`, `.cut()`, `.getCode()`, `.getElement()`, `.setElement()`, `.setValue()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Form Components`** (6 nodes): `SelectionObserver`, `._addEvent()`, `._move()`, `.removeEvent()`, `._startMove()`, `._stopMove()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `API Layer`** (6 nodes): `accountInfoApi()`, `sessionTimeoutApi()`, `tokenExpiredApi()`, `setup()`, `account.ts`, `index.vue`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `ControlBorder`** (5 nodes): `ControlBorder`, `.clearBorderInfo()`, `.constructor()`, `.recordBorderInfo()`, `.render()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `WorkerManager`** (5 nodes): `WorkerManager`, `.constructor()`, `.getCatalog()`, `.getGroupIds()`, `.getWordCount()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `User Module`** (3 nodes): `UserService`, `.upload()`, `FileService.ts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `VideoBlock`** (3 nodes): `VideoBlock`, `.constructor()`, `.render()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Composables/Hooks`** (3 nodes): `Plugin`, `.constructor()`, `.use()`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Small 92`** (2 nodes): `FileController`, `FileController.ts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `User Module`** (2 nodes): `UserController`, `UserController.ts`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.
- **Thin community `Configuration`** (2 nodes): `pagination.ts`, `PaginationConfig`
  Too small to be a meaningful cluster - may be noise or needs more connections extracted.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `useMessage()` connect `setup()` to `API Layer`, `createAxios()`, `Table/List Components`, `Table/List Components`, `Form Components`, `Dictionary`, `Vue Components`, `User Module`, `Table/List Components`, `Memory`, `Auth/Login`, `if()`, `Form Components`, `Form Components`, `getRandom()`, `API Layer`, `Configuration`, `batchHandleDelete()`, `Table/List Components`?**
  _High betweenness centrality (0.175) - this node is a cross-community bridge._
- **Why does `useDesign()` connect `createAxios()` to `API Layer`, `setup()`, `Table/List Components`, `Form Components`, `Dictionary`, `User Module`, `Configuration`, `getRandom()`?**
  _High betweenness centrality (0.056) - this node is a cross-community bridge._
- **Why does `nextTick()` connect `Configuration` to `User Module`, `API Layer`, `createAxios()`, `Dictionary`, `Memory`, `Form Components`?**
  _High betweenness centrality (0.055) - this node is a cross-community bridge._
- **Are the 43 inferred relationships involving `useMessage()` (e.g. with `useCommentWithFile()` and `useColumnsCache()`) actually correct?**
  _`useMessage()` has 43 INFERRED edges - model-reasoned connections that need verification._
- **Are the 107 inferred relationships involving `unref()` (e.g. with `getValueType()` and `vModel()`) actually correct?**
  _`unref()` has 107 INFERRED edges - model-reasoned connections that need verification._
- **Are the 32 inferred relationships involving `useI18n()` (e.g. with `translateTitle()` and `checkStatus()`) actually correct?**
  _`useI18n()` has 32 INFERRED edges - model-reasoned connections that need verification._
- **What connects `FileController`, `UserController`, `PaginationConfig` to the rest of the system?**
  _4 weakly-connected nodes found - possible documentation gaps or missing edges._