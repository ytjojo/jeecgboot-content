<template>
  <a-row :gutter="[16, 16]">
    <a-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
      <a-card class="overview-card" :bordered="false">
        <div class="card-content">
          <div class="card-icon exp-icon">
            <ThunderboltOutlined />
          </div>
          <div class="card-info">
            <div class="card-label">经验值</div>
            <div class="card-value">
              <CountTo :end-val="expPoints" :duration="1500" />
            </div>
          </div>
        </div>
      </a-card>
    </a-col>
    <a-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
      <a-card class="overview-card" :bordered="false">
        <div class="card-content">
          <div class="card-icon contribution-icon">
            <StarOutlined />
          </div>
          <div class="card-info">
            <div class="card-label">贡献值</div>
            <div class="card-value">
              <CountTo :end-val="contributionPoints" :duration="1500" />
            </div>
          </div>
        </div>
      </a-card>
    </a-col>
    <a-col :xs="24" :sm="24" :md="8" :lg="8" :xl="8">
      <a-card class="overview-card" :bordered="false">
        <div class="card-content">
          <div class="card-icon rank-icon">
            <TrophyOutlined />
          </div>
          <div class="card-info">
            <div class="card-label">圈内排名</div>
            <div class="card-value">
              <CountTo :end-val="rank" :duration="1500" prefix="#" />
            </div>
          </div>
        </div>
      </a-card>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
  import { ThunderboltOutlined, StarOutlined, TrophyOutlined } from '@ant-design/icons-vue';
  import { CountTo } from '/@/components/CountTo';

  interface Props {
    expPoints: number;
    contributionPoints: number;
    rank: number;
  }

  defineProps<Props>();
</script>

<style lang="less" scoped>
  .overview-card {
    margin-bottom: 16px;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    }
  }

  .card-content {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .card-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    flex-shrink: 0;
  }

  .exp-icon {
    background: linear-gradient(135deg, #fff7e6 0%, #ffe7ba 100%);
    color: #fa8c16;
  }

  .contribution-icon {
    background: linear-gradient(135deg, #e6f7ff 0%, #bae7ff 100%);
    color: #1890ff;
  }

  .rank-icon {
    background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
    color: #52c41a;
  }

  .card-info {
    flex: 1;
    min-width: 0;
  }

  .card-label {
    font-size: 14px;
    color: var(--text-color-secondary, rgba(0, 0, 0, 0.45));
    margin-bottom: 4px;
  }

  .card-value {
    font-size: 28px;
    font-weight: 700;
    color: var(--text-color, rgba(0, 0, 0, 0.85));
    line-height: 1.2;
  }

  @media (max-width: 768px) {
    .card-icon {
      width: 48px;
      height: 48px;
      font-size: 24px;
    }

    .card-value {
      font-size: 24px;
    }
  }
</style>
