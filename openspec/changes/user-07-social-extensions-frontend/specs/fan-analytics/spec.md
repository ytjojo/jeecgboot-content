## ADDED Requirements

### Requirement: Fan list page with pagination and search
The system SHALL provide a fan management page at `/content/fan` with a tabbed interface containing fan list and fan trend views.

#### Scenario: View fan list
- **WHEN** user navigates to the fan management page
- **THEN** the system SHALL display the "列表" tab by default, showing fan statistics (total fans, today's new), a search bar, and a paginated table with columns: avatar, nickname, follow time, interaction count
  - **验收标准**: 列表加载 < 1s（分页每页 20 条）

#### Scenario: Search fans by nickname
- **WHEN** user types a keyword in the fan list search bar
- **THEN** the system SHALL filter the list by nickname and update results

#### Scenario: Empty fan list
- **WHEN** user has no fans
- **THEN** the system SHALL display an empty state with text "还没有粉丝，发布优质内容吸引更多关注吧"

### Requirement: Fan trend chart
The system SHALL provide a fan trend tab with ECharts line chart supporting day/week/month dimension switching.

#### Scenario: View fan trend by day
- **WHEN** user switches to the "趋势" tab and selects "日" dimension
- **THEN** the system SHALL display a line chart showing daily new fan counts for the last 30 days
  - **验收标准**: 图表首次渲染 < 2s（ECharts 按需加载）

#### Scenario: Switch trend dimension
- **WHEN** user switches between day/week/month dimensions using Radio.Group
- **THEN** the system SHALL re-fetch trend data with the selected dimension and update the chart

#### Scenario: Click trend data point for details
- **WHEN** user clicks a data point on the trend chart
- **THEN** the system SHALL display a Modal showing the list of new fans for that date

#### Scenario: Fan trend loading
- **WHEN** the trend data is loading
- **THEN** the system SHALL display a Spin loading indicator in the chart area

### Requirement: Fan analytics API integration
The system SHALL integrate fan analytics APIs using `defHttp` encapsulation in `src/api/content/fan-analytics.ts`.

#### Scenario: Fetch fan list
- **WHEN** the fan list tab loads or user searches/paginates
- **THEN** the system SHALL call `GET /api/v1/content/user/fan/list` with pagination and search params

#### Scenario: Fetch fan trend data
- **WHEN** user switches to the trend tab or changes dimension
- **THEN** the system SHALL call `GET /api/v1/content/user/fan/trend` with dimension (day/week/month) and optional date range params

### Requirement: ECharts lazy loading
The system SHALL use `echarts/core` with on-demand chart type registration to minimize bundle size.

#### Scenario: ECharts bundle optimization
- **WHEN** the application builds
- **THEN** the echarts bundle SHALL only include the line chart type and required components (not the full echarts library)

## API 封装

API 文件: `src/api/content/fan-analytics.ts`

| 端点 | 方法 | 参数 | 响应关键字段 | 状态 |
|------|------|------|------------|------|
| `/api/v1/content/user/fan/list` | GET | @RequestParam: page, pageSize, keyword? | records[{avatar, nickname, followTime, interactionCount}], total, totalFans, todayNew | ✅ 后端已实现 |
| `/api/v1/content/user/fan/trend` | GET | @RequestParam: dimension(day/week/month), startDate?, endDate? | [{date, count}] | ✅ 后端已实现 |
| `/api/v1/content/user/fan/profile` | GET | 无参数（基于当前用户） | interests[], regions[], activeHours[], totalFans | ⏸️ 降级至二期 |
| `/api/v1/content/user/fan/export` | POST | 无参数 | CSV 文件流 | ⏸️ 降级至二期 |
