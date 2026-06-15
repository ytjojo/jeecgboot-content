每個 Claude Code 工作階段都以全新的 context window 開始。兩個機制可以跨工作階段傳遞知識：

-   **CLAUDE.md 檔案**：您編寫的指令，為 Claude 提供持久的上下文
-   **自動記憶**：Claude 根據您的更正和偏好自己編寫的筆記

本頁涵蓋如何：

-   [編寫和組織 CLAUDE.md 檔案](https://code.claude.com/docs/zh-TW/memory#claude-md-files)
-   [使用 `.claude/rules/` 將規則範圍限定於特定檔案類型](https://code.claude.com/docs/zh-TW/memory#organize-rules-with-clauderules)
-   [配置自動記憶](https://code.claude.com/docs/zh-TW/memory#auto-memory)，使 Claude 自動記筆記
-   [疑難排解](https://code.claude.com/docs/zh-TW/memory#troubleshoot-memory-issues)指令未被遵循的情況

Claude Code 有兩個互補的記憶系統。兩者都在每次對話開始時載入。Claude 將它們視為上下文，而不是強制配置。若要阻止某個動作（無論 Claude 決定什麼），請改用 [PreToolUse hook](https://code.claude.com/docs/zh-TW/hooks-guide)。您的指令越具體和簡潔，Claude 遵循它們的一致性就越高。

|  | CLAUDE.md 檔案 | 自動記憶 |
|------|----------------|------------------------|
| **誰編寫** | 您 | Claude |
| **包含內容** | 指令和規則 | 學習和模式 |
| **範圍** | 專案、使用者或組織 | 每個工作樹，跨 worktrees 共享 |
| **載入到** | 每個工作階段 | 每個工作階段（前 200 行或 25KB） |
| **用於** | 編碼標準、工作流程、專案架構 | 建置命令、除錯見解、Claude 發現的偏好 |

當您想引導 Claude 的行為時，使用 CLAUDE.md 檔案。自動記憶讓 Claude 從您的更正中學習，無需手動操作。 Subagents 也可以維護自己的自動記憶。有關詳細資訊，請參閱 [subagent 配置](https://code.claude.com/docs/zh-TW/sub-agents#enable-persistent-memory)。

## CLAUDE.md 檔案

CLAUDE.md 檔案是 markdown 檔案，為專案、您的個人工作流程或整個組織為 Claude 提供持久指令。您以純文字編寫這些檔案；Claude 在每個工作階段開始時讀取它們。

### 何時新增到 CLAUDE.md

將 CLAUDE.md 視為您寫下您本來會重新解釋的內容的地方。在以下情況下新增到它：

-   Claude 第二次犯同樣的錯誤
-   程式碼審查發現 Claude 應該知道的關於此程式碼庫的內容
-   您在聊天中輸入的相同更正或澄清是您上個工作階段輸入的
-   新的團隊成員需要相同的上下文才能提高生產力

將其保持為 Claude 應該在每個工作階段中保留的事實：建置命令、慣例、專案佈局、「始終執行 X」規則。如果一個條目是多步驟程序或僅對程式碼庫的一部分重要，請將其移到 [skill](https://code.claude.com/docs/zh-TW/skills) 或 [路徑範圍規則](https://code.claude.com/docs/zh-TW/memory#organize-rules-with-claude/rules/) 代替。[擴展概述](https://code.claude.com/docs/zh-TW/features-overview#build-your-setup-over-time)涵蓋何時使用每個機制。

### 選擇 CLAUDE.md 檔案的位置

CLAUDE.md 檔案可以位於多個位置，每個位置都有不同的範圍。下表按載入順序列出它們，從最廣泛的範圍到最具體的範圍，因此專案指令在使用者指令之後出現在上下文中。

| 範圍 | 位置 | 目的 | 使用案例示例 | 共享對象 |
|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------|------------------|--------------|
| **受管理的原則** | • macOS：`/Library/Application Support/ClaudeCode/CLAUDE.md`<br>• Linux 和 WSL：`/etc/claude-code/CLAUDE.md`<br>• Windows：`C:\Program Files\ClaudeCode\CLAUDE.md` | 由 IT/DevOps 管理的組織範圍指令 | 公司編碼標準、安全原則、合規要求 | 組織中的所有使用者 |
| **使用者指令** | `~/.claude/CLAUDE.md` | 所有專案的個人偏好 | 程式碼樣式偏好、個人工具快捷方式 | 僅您（所有專案） |
| **專案指令** | `./CLAUDE.md` 或 `./.claude/CLAUDE.md` | 專案的團隊共享指令 | 專案架構、編碼標準、常見工作流程 | 透過原始碼控制的團隊成員 |
| **本地指令** | `./CLAUDE.local.md` | 個人專案特定偏好；新增到 `.gitignore` | 您的沙箱 URL、偏好的測試資料 | 僅您（目前專案） |

工作目錄上方目錄層級中的 CLAUDE.md 和 CLAUDE.local.md 檔案在啟動時完整載入。子目錄中的檔案在 Claude 讀取這些目錄中的檔案時按需載入。有關完整的解析順序，請參閱 [CLAUDE.md 檔案如何載入](https://code.claude.com/docs/zh-TW/memory#how-claude-md-files-load)。 對於大型專案，您可以使用 [專案規則](https://code.claude.com/docs/zh-TW/memory#organize-rules-with-claude/rules/) 將指令分解為主題特定的檔案。規則讓您將指令範圍限定於特定檔案類型或子目錄。

### 設定專案 CLAUDE.md

專案 CLAUDE.md 可以儲存在 `./CLAUDE.md` 或 `./.claude/CLAUDE.md` 中。建立此檔案並新增適用於在專案上工作的任何人的指令：建置和測試命令、編碼標準、架構決策、命名慣例和常見工作流程。這些指令透過版本控制與您的團隊共享，因此請專注於專案級別的標準，而不是個人偏好。

### 編寫有效的指令

CLAUDE.md 檔案在每個工作階段開始時載入到 context window 中，與您的對話一起消耗令牌。[context window 視覺化](https://code.claude.com/docs/zh-TW/context-window)顯示 CLAUDE.md 相對於其餘啟動上下文的載入位置。因為它們是上下文而不是強制配置，您編寫指令的方式會影響 Claude 遵循它們的可靠性。具體、簡潔、結構良好的指令效果最好。 **大小**：目標是每個 CLAUDE.md 檔案少於 200 行。較長的檔案消耗更多上下文並降低遵守度。如果您的指令變得很大，請使用 [路徑範圍規則](https://code.claude.com/docs/zh-TW/memory#path-specific-rules) 以便指令只在 Claude 處理匹配檔案時載入。您也可以將內容分割成 [匯入](https://code.claude.com/docs/zh-TW/memory#import-additional-files) 以進行組織，儘管匯入的檔案仍然會載入並在啟動時進入 context window。 **結構**：使用 markdown 標題和項目符號來分組相關指令。Claude 掃描結構的方式與讀者相同：組織良好的部分比密集的段落更容易遵循。 **具體性**：編寫具體到足以驗證的指令。例如：

-   「使用 2 空格縮排」而不是「正確格式化程式碼」
-   「在提交前執行 `npm test`」而不是「測試您的變更」
-   「API 處理程式位於 `src/api/handlers/`」而不是「保持檔案組織」

**一致性**：如果兩個規則相互矛盾，Claude 可能會任意選擇一個。定期檢查您的 CLAUDE.md 檔案、子目錄中的巢狀 CLAUDE.md 檔案和 [`.claude/rules/`](https://code.claude.com/docs/zh-TW/memory#organize-rules-with-claude/rules/)，以移除過時或衝突的指令。在 monorepos 中，使用 [`claudeMdExcludes`](https://code.claude.com/docs/zh-TW/memory#exclude-specific-claude-md-files) 跳過與您的工作無關的其他團隊的 CLAUDE.md 檔案。

### 匯入其他檔案

CLAUDE.md 檔案可以使用 `@path/to/import` 語法匯入其他檔案。匯入的檔案會展開並在啟動時與參考它們的 CLAUDE.md 一起載入到上下文中。 允許相對和絕對路徑。相對路徑相對於包含匯入的檔案解析，而不是工作目錄。匯入的檔案可以遞迴匯入其他檔案，最大深度為四跳。 要引入 README、package.json 和工作流程指南，請在 CLAUDE.md 中的任何位置使用 `@` 語法參考它們：

```less
有關專案概述，請參閱 @README，有關此專案的可用 npm 命令，請參閱 @package.json。

# 其他指令
- git 工作流程 @docs/git-instructions.md
```

對於您不想簽入版本控制的個人偏好，請在專案根目錄建立 `CLAUDE.local.md`。它與 `CLAUDE.md` 一起載入並以相同方式處理。將 `CLAUDE.local.md` 新增到您的 `.gitignore`，以便不提交它；執行 `/init` 並選擇個人選項會為您執行此操作。 如果您在同一儲存庫的多個 git worktrees 中工作，gitignored 的 `CLAUDE.local.md` 只存在於您建立它的 worktree 中。要在 worktrees 之間共享個人指令，請改為從您的主目錄匯入檔案：

```bash
# 個人偏好
- @~/.claude/my-project-instructions.md
```

有關組織指令的更結構化方法，請參閱 [`.claude/rules/`](https://code.claude.com/docs/zh-TW/memory#organize-rules-with-claude/rules/)。

### AGENTS.md

Claude Code 讀取 `CLAUDE.md`，而不是 `AGENTS.md`。如果您的儲存庫已經為其他編碼代理使用 `AGENTS.md`，請建立一個 `CLAUDE.md` 來匯入它，以便兩個工具讀取相同的指令而不重複。您也可以在匯入下方新增 Claude 特定的指令。Claude 在工作階段開始時載入匯入的檔案，然後附加其餘部分：

CLAUDE.md

```perl
@AGENTS.md

## Claude Code

對 `src/billing/` 下的變更使用 plan mode。
```

符號連結也可以運作，如果您不需要新增 Claude 特定的內容：

```bash
ln -s AGENTS.md CLAUDE.md
```

在 Windows 上，建立符號連結需要系統管理員權限或開發人員模式，因此請改用 `@AGENTS.md` 匯入。 在已經有 `AGENTS.md` 的儲存庫中執行 [`/init`](https://code.claude.com/docs/zh-TW/commands) 會讀取它並將相關部分合併到產生的 `CLAUDE.md` 中。它也會讀取其他工具配置，如 `.cursorrules` 和 `.windsurfrules`。

### CLAUDE.md 檔案如何載入

Claude Code 透過從您目前的工作目錄向上走目錄樹來讀取 CLAUDE.md 檔案，檢查沿途的每個目錄中是否有 `CLAUDE.md` 和 `CLAUDE.local.md` 檔案。這意味著如果您在 `foo/bar/` 中執行 Claude Code，它會從 `foo/bar/CLAUDE.md`、`foo/CLAUDE.md` 和沿途的任何 `CLAUDE.local.md` 檔案載入指令。 所有發現的檔案都被連接到上下文中，而不是相互覆蓋。在目錄樹中，內容按照從檔案系統根目錄到您的工作目錄的順序排列。對於 `foo/bar/` 示例，`foo/CLAUDE.md` 在上下文中出現在 `foo/bar/CLAUDE.md` 之前，因此更接近您啟動 Claude 的位置的指令最後被讀取。在每個目錄中，`CLAUDE.local.md` 附加在 `CLAUDE.md` 之後，因此您的個人筆記是 Claude 在該級別讀取的最後一件事。 Claude 也會在您目前工作目錄下的子目錄中發現 `CLAUDE.md` 和 `CLAUDE.local.md` 檔案。它們不是在啟動時載入，而是在 Claude 讀取這些子目錄中的檔案時包含。 如果您在大型 monorepo 中工作，其中其他團隊的 CLAUDE.md 檔案被拾取，請使用 [`claudeMdExcludes`](https://code.claude.com/docs/zh-TW/memory#exclude-specific-claude-md-files) 跳過它們。對於根目錄和每個目錄 CLAUDE.md 檔案和規則的完整佈局，請參閱 [Monorepos 和大型儲存庫](https://code.claude.com/docs/zh-TW/large-codebases)。 CLAUDE.md 檔案中的區塊級 HTML 註解（`<!-- maintainer notes -->`）在內容被注入到 Claude 的上下文之前會被移除。使用它們為人類維護者留下筆記，而不會在令牌上花費上下文。程式碼區塊內的註解會被保留。當您直接使用 Read 工具開啟 CLAUDE.md 檔案時，註解保持可見。

#### 從其他目錄載入

`--add-dir` 旗標讓 Claude 可以存取主工作目錄外的其他目錄。預設情況下，不會載入這些目錄中的 CLAUDE.md 檔案。 要也從其他目錄載入記憶檔案，請設定 `CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD` 環境變數：

```ini
CLAUDE_CODE_ADDITIONAL_DIRECTORIES_CLAUDE_MD=1 claude --add-dir ../shared-config
```

這會從其他目錄載入 `CLAUDE.md`、`.claude/CLAUDE.md`、`.claude/rules/*.md` 和 `CLAUDE.local.md`。如果您從 [`--setting-sources`](https://code.claude.com/docs/zh-TW/cli-reference) 排除 `local`，則會跳過 `CLAUDE.local.md`。

### 使用 `.claude/rules/` 組織規則

對於較大的專案，您可以使用 `.claude/rules/` 目錄將指令組織成多個檔案。這使指令保持模組化，更容易讓團隊維護。規則也可以 [範圍限定於特定檔案路徑](https://code.claude.com/docs/zh-TW/memory#path-specific-rules)，因此它們只在 Claude 處理匹配檔案時載入到上下文中，減少雜訊並節省上下文空間。

#### 設定規則

在您的專案的 `.claude/rules/` 目錄中放置 markdown 檔案。每個檔案應涵蓋一個主題，具有描述性檔案名稱，如 `testing.md` 或 `api-design.md`。所有 `.md` 檔案都被遞迴發現，因此您可以將規則組織到子目錄中，如 `frontend/` 或 `backend/`：

```bash
your-project/
├── .claude/
│   ├── CLAUDE.md           # 主要專案指令
│   └── rules/
│       ├── code-style.md   # 程式碼樣式指南
│       ├── testing.md      # 測試慣例
│       └── security.md     # 安全要求
```

沒有 [`paths` frontmatter](https://code.claude.com/docs/zh-TW/memory#path-specific-rules) 的規則在啟動時載入，優先級與 `.claude/CLAUDE.md` 相同。

#### 路徑特定規則

規則可以使用帶有 `paths` 欄位的 YAML frontmatter 範圍限定於特定檔案。這些條件規則僅在 Claude 處理與指定模式匹配的檔案時適用。

```yaml
---
paths:
  - "src/api/**/*.ts"
---

# API 開發規則

- 所有 API 端點必須包括輸入驗證
- 使用標準錯誤回應格式
- 包括 OpenAPI 文件註解
```

沒有 `paths` 欄位的規則無條件載入並適用於所有檔案。路徑範圍規則在 Claude 讀取與模式匹配的檔案時觸發，而不是在每次工具使用時觸發。 在 `paths` 欄位中使用 glob 模式，按副檔名、目錄或任何組合匹配檔案：

| 模式 | 匹配 |
|----------------------|------------------------|
| `**/*.ts` | 任何目錄中的所有 TypeScript 檔案 |
| `src/**/*` | `src/` 目錄下的所有檔案 |
| `*.md` | 專案根目錄中的 Markdown 檔案 |
| `src/components/*.tsx` | 特定目錄中的 React 元件 |

您可以指定多個模式並使用大括號展開在一個模式中匹配多個副檔名：

```yaml
---
paths:
  - "src/**/*.{ts,tsx}"
  - "lib/**/*.ts"
  - "tests/**/*.test.ts"
---
```

#### 使用符號連結跨專案共享規則

`.claude/rules/` 目錄支援符號連結，因此您可以維護一組共享規則並將它們連結到多個專案中。符號連結被解析並正常載入，並且循環符號連結被檢測並妥善處理。 此示例連結共享目錄和單個檔案：

```bash
ln -s ~/shared-claude-rules .claude/rules/shared
ln -s ~/company-standards/security.md .claude/rules/security.md
```

#### 使用者級別規則

`~/.claude/rules/` 中的個人規則適用於您機器上的每個專案。使用它們來處理不是專案特定的偏好：

```bash
~/.claude/rules/
├── preferences.md    # 您的個人編碼偏好
└── workflows.md      # 您偏好的工作流程
```

使用者級別規則在專案規則之前載入，給予專案規則更高的優先級。

### 為大型團隊管理 CLAUDE.md

對於在團隊中部署 Claude Code 的組織，您可以集中指令並控制載入哪些 CLAUDE.md 檔案。

#### 部署組織範圍的 CLAUDE.md

組織可以部署一個集中管理的 CLAUDE.md，適用於機器上的所有使用者。此檔案無法被個人設定排除。

1

2

`claudeMd` 金鑰讓您將受管理的 CLAUDE.md 內容直接放入 `managed-settings.json` 中，而不是部署單獨的檔案。 **範圍**：機器上的每個 Claude Code 工作階段，在每個儲存庫中。對於儲存庫特定的指導，請改為提交專案 CLAUDE.md。 **優先級**：與受管理的 CLAUDE.md 檔案相同。在使用者和專案 CLAUDE.md 之前載入。 **在何處被遵守**：僅受管理和原則設定。在使用者、專案或本地設定中設定 `claudeMd` 無效。 下面的示例直接在受管理的設定檔案中新增行為指令：

```swift
{
  "claudeMd": "Always run `make lint` before committing.\nNever push directly to main."
}
```

受管理的 CLAUDE.md 和 [受管理的設定](https://code.claude.com/docs/zh-TW/settings#settings-files) 有不同的用途。使用設定進行技術強制執行，使用 CLAUDE.md 進行行為指導：

| 關注 | 配置在 |
|-----------------|-------------------------------------------|
| 阻止特定工具、命令或檔案路徑 | 受管理的設定：`permissions.deny` |
| 強制執行沙箱隔離 | 受管理的設定：`sandbox.enabled` |
| 環境變數和 API 提供者路由 | 受管理的設定：`env` |
| 驗證方法和組織鎖定 | 受管理的設定：`forceLoginMethod`、`forceLoginOrgUUID` |
| 程式碼樣式和品質指南 | 受管理的 CLAUDE.md |
| 資料處理和合規提醒 | 受管理的 CLAUDE.md |
| Claude 的行為指令 | 受管理的 CLAUDE.md |

設定規則由用戶端強制執行，無論 Claude 決定做什麼。CLAUDE.md 指令塑造 Claude 的行為，但不是硬強制執行層。

#### 排除特定的 CLAUDE.md 檔案

在大型 monorepos 中，祖先 CLAUDE.md 檔案可能包含與您的工作無關的指令。`claudeMdExcludes` 設定讓您按路徑或 glob 模式跳過特定檔案。 此示例排除頂級 CLAUDE.md 和父資料夾中的規則目錄。將其新增到 `.claude/settings.local.json`，以便排除保留在您的機器本地：

```json
{
  "claudeMdExcludes": [
    "**/monorepo/CLAUDE.md",
    "/home/user/monorepo/other-team/.claude/rules/**"
  ]
}
```

模式使用 glob 語法與絕對檔案路徑匹配。您可以在任何 [設定層](https://code.claude.com/docs/zh-TW/settings#settings-files)：使用者、專案、本地或受管理的原則配置 `claudeMdExcludes`。陣列跨層合併。 受管理的原則 CLAUDE.md 檔案無法被排除。這確保組織範圍的指令始終適用，無論個人設定如何。

## 自動記憶

自動記憶讓 Claude 在您不編寫任何內容的情況下跨工作階段累積知識。Claude 在工作時為自己保存筆記：建置命令、除錯見解、架構筆記、程式碼樣式偏好和工作流程習慣。Claude 不會每個工作階段都保存內容。它根據資訊在未來對話中是否有用來決定值得記住的內容。

### 啟用或停用自動記憶

自動記憶預設為開啟。要切換它，請在工作階段中開啟 `/memory` 並使用自動記憶切換，或在您的專案設定中設定 `autoMemoryEnabled`：

```json
{
  "autoMemoryEnabled": false
}
```

要透過環境變數停用自動記憶，請設定 `CLAUDE_CODE_DISABLE_AUTO_MEMORY=1`。

### 儲存位置

每個專案在 `~/.claude/projects/<project>/memory/` 獲得自己的記憶目錄。`<project>` 路徑源自 git 儲存庫，因此同一儲存庫內的所有 worktrees 和子目錄共享一個自動記憶目錄。在 git 儲存庫外，改用專案根目錄。 要將自動記憶儲存在不同位置，請在您的 `settings.json` 中設定 `autoMemoryDirectory`。它從任何[設定範圍](https://code.claude.com/docs/zh-TW/settings#settings-precedence)讀取：使用者、專案、本地、原則或 `--settings`。

```json
{
  "autoMemoryDirectory": "~/my-custom-memory-dir"
}
```

此值必須是絕對路徑或以 `~/` 開頭。當在專案的 `.claude/settings.json` 或 `.claude/settings.local.json` 中設定時，此值僅在您接受該資料夾的工作區信任對話後才會被接受，這與管理 hooks 的閘道相同。 目錄包含 `MEMORY.md` 進入點和可選的主題檔案：

```bash
~/.claude/projects/<project>/memory/
├── MEMORY.md          # 簡潔索引，載入到每個工作階段
├── debugging.md       # 除錯模式的詳細筆記
├── api-conventions.md # API 設計決策
└── ...                # Claude 建立的任何其他主題檔案
```

`MEMORY.md` 充當記憶目錄的索引。Claude 在您的工作階段中讀取和寫入此目錄中的檔案，使用 `MEMORY.md` 追蹤儲存的內容。 自動記憶是機器本地的。同一 git 儲存庫內的所有 worktrees 和子目錄共享一個自動記憶目錄。檔案不在機器或雲端環境之間共享。

### 它如何運作

`MEMORY.md` 的前 200 行或前 25KB（以先到者為準）在每次對話開始時載入。超過該閾值的內容在工作階段開始時不載入。Claude 透過將詳細筆記移到單獨的主題檔案中來保持 `MEMORY.md` 簡潔。 此限制僅適用於 `MEMORY.md`。CLAUDE.md 檔案無論長度如何都完整載入，儘管較短的檔案會產生更好的遵守度。 主題檔案如 `debugging.md` 或 `patterns.md` 在啟動時不載入。Claude 在需要資訊時使用其標準檔案工具按需讀取它們。 Claude 在您的工作階段中讀取和寫入記憶檔案。當您在 Claude Code 介面中看到「寫入記憶」或「回憶記憶」時，Claude 正在主動更新或讀取 `~/.claude/projects/<project>/memory/`。

### 審計和編輯您的記憶

自動記憶檔案是純 markdown，您可以隨時編輯或刪除。執行 [`/memory`](https://code.claude.com/docs/zh-TW/memory#view-and-edit-with-memory) 以從工作階段中瀏覽和開啟記憶檔案。

## 使用 `/memory` 檢視和編輯

`/memory` 命令列出在您目前工作階段中載入的所有 CLAUDE.md、CLAUDE.local.md 和規則檔案，讓您切換自動記憶開啟或關閉，並提供開啟自動記憶資料夾的連結。選擇任何檔案以在您的編輯器中開啟它。 當您要求 Claude 記住某些內容時，例如「始終使用 pnpm，而不是 npm」或「記住 API 測試需要本地 Redis 實例」，Claude 會將其保存到自動記憶。要改為將指令新增到 CLAUDE.md，請直接要求 Claude，例如「將此新增到 CLAUDE.md」，或透過 `/memory` 自己編輯檔案。

## 疑難排解記憶問題

這些是 CLAUDE.md 和自動記憶最常見的問題，以及除錯步驟。

### Claude 不遵循我的 CLAUDE.md

CLAUDE.md 內容作為系統提示後的使用者訊息傳遞，而不是系統提示本身的一部分。Claude 讀取它並嘗試遵循它，但沒有嚴格遵守的保證，特別是對於模糊或衝突的指令。 要除錯：

-   執行 `/memory` 以驗證您的 CLAUDE.md 和 CLAUDE.local.md 檔案是否被載入。如果檔案未列出，Claude 看不到它。
-   檢查相關的 CLAUDE.md 是否位於為您的工作階段載入的位置（請參閱 [選擇 CLAUDE.md 檔案的位置](https://code.claude.com/docs/zh-TW/memory#choose-where-to-put-claude-md-files)）。
-   使指令更具體。「使用 2 空格縮排」比「正確格式化程式碼」效果更好。
-   查找跨 CLAUDE.md 檔案的衝突指令。如果兩個檔案為相同行為提供不同的指導，Claude 可能會任意選擇一個。

如果指令是必須在特定時間點執行的內容，例如在每次提交前或每次檔案編輯後，請改為將其寫成 [hook](https://code.claude.com/docs/zh-TW/hooks-guide)。Hooks 在固定的生命週期事件中作為 shell 命令執行，並且無論 Claude 決定做什麼都適用。 對於您想要在系統提示級別的指令，請使用 [`--append-system-prompt`](https://code.claude.com/docs/zh-TW/cli-reference#system-prompt-flags)。這必須在每次呼叫時傳遞，因此它更適合指令碼和自動化，而不是互動式使用。

### 我不知道自動記憶保存了什麼

執行 `/memory` 並選擇自動記憶資料夾以瀏覽 Claude 保存的內容。一切都是純 markdown，您可以讀取、編輯或刪除。

### 我的 CLAUDE.md 太大了

超過 200 行的檔案消耗更多上下文，可能會降低遵守度。使用 [路徑範圍規則](https://code.claude.com/docs/zh-TW/memory#path-specific-rules) 僅在 Claude 處理符合的檔案時載入指令，或修剪不是每個工作階段都需要的內容。分割成 [`@path` 匯入](https://code.claude.com/docs/zh-TW/memory#import-additional-files) 有助於組織，但不會減少上下文，因為匯入的檔案在啟動時載入。

### 指令在 `/compact` 後似乎丟失了

專案根目錄 CLAUDE.md 在壓縮中倖存：在 `/compact` 之後，Claude 從磁碟重新讀取它並將其重新注入到工作階段中。子目錄中的巢狀 CLAUDE.md 檔案不會自動重新注入；它們在 Claude 下次讀取該子目錄中的檔案時重新載入。 如果指令在壓縮後消失，它要麼只在對話中給出，要麼位於尚未重新載入的巢狀 CLAUDE.md 中。將對話專用指令新增到 CLAUDE.md 以使其持久化。有關完整的細目，請參閱 [壓縮後倖存的內容](https://code.claude.com/docs/zh-TW/context-window#what-survives-compaction)。 請參閱 [編寫有效的指令](https://code.claude.com/docs/zh-TW/memory#write-effective-instructions) 以取得有關大小、結構和具體性的指導。

## 相關資源

-   [除錯您的配置](https://code.claude.com/docs/zh-TW/debug-your-config)：診斷為什麼 CLAUDE.md 或設定未生效
-   [Skills](https://code.claude.com/docs/zh-TW/skills)：封裝按需載入的可重複工作流程
-   [設定](https://code.claude.com/docs/zh-TW/settings)：使用設定檔案配置 Claude Code 行為
-   [Subagent 記憶](https://code.claude.com/docs/zh-TW/sub-agents#enable-persistent-memory)：讓 subagents 維護自己的自動記憶