**启用联网搜索 MCP**

`claude mcp add-json web-search-prime  '{"type":"http", "url":"https://open.bigmodel.cn/api/mcp/web_search_prime/mcp","headers":{"Authorization":"Bearer <你的 GLM API Key>"}}'   --scope user`

**启用图像/视频理解 MCP**

`claude mcp add-json zai-mcp-server  '{"type":"stdio","command":"cmd","args":["/c","npx","-y","@z_ai/mcp-server"],"env":{"Z_AI_API_KEY":"<你的 GLM API Key>"}}'  --scope user`

**谷歌浏览器开发工具**

`claude mcp add chrome-devtools npx chrome-devtools-mcp@latest  --scope user`

**Filesystem MCP —— 读写文件系统**

让 Claude 能访问你指定的本地文件夹。

`claude mcp add fs -- npx -y @modelcontextprotocol/server-filesystem ~/Projects`

**Playwright MCP —— 浏览器自动化**

`claude mcp add playwright -- npx -y @playwright/mcp@latest --scope use`

**GitHub MCP —— 远程仓库协作**
Claude 可直接连接到 GitHub 的 PR 和 Issue。

`claude mcp add github \ --env GITHUB_PERSONAL_ACCESS_TOKEN=ghp_xxx \ -- npx -y @modelcontextprotocol/server-github`

**使用场景：**拉取 PR → Claude 自动生成 Review 意见；根据 Issue 描述 → 生成修复思路；查询仓库最新版本

**Sentry MCP —— 线上监控日志**
让 Claude 直接读取错误监控数据。

`claude mcp add --transport http sentry https://mcp.sentry.dev/mcp`

**使用场景：**查询近期最频繁的报错；追踪错误堆栈并生成排障建议；按版本统计错误趋势

**Vercel MCP —— 部署与环境**
Claude 可直接与 Vercel 平台交互。

`claude mcp add --transport http vercel https://mcp.vercel.com/`

**Context 7 MCP —— 实时技术文档**
`claude mcp add context7 -- npx -y @context7/mcp-server`

**Sequential Thinking 多步推理服务器**

`claude mcp add sequential-thinking -s user -- npx -y @modelcontextprotocol/server-sequential-thinking@latest`

**Repomix/DeepWiki MCP**
`claude mcp add mcp-deepwiki -- npx -y mcp-deepwiki --scope user`

**Figma MCP**
`claude mcp add --transport http figma-remote-mcp https://mcp.figma.com/mcp --scope user`

**Git MCP**
`claude mcp add git -- uvx --from git+https://github.com/modelcontextprotocol/servers.git mcp-server-git --scope user`

**Task Master**
`claude mcp add task-master-ai -- npx -y task-master-ai --scope user`

**BlenderMCP**
`claude mcp add blender-mcp -- npx -y blender-mcp --scope user`

**mysqlMCP**
`claude mcp add mcp_server_mysql -e MYSQL_HOST="127.0.0.1" -e MYSQL_PORT="3306" -e MYSQL_USER="root" -e MYSQL_PASS="root" -e MYSQL_DB="jeecg-boot" -- npx @benborla29/mcp-server-mysql --scope user`

**Serena**
`claude mcp add serena -- uvx --from git+https://github.com/oraios/serena serena start-mcp-server --context ide-assistant --project $(pwd) --scope user`
