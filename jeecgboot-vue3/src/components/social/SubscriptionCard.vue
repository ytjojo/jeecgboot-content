<template>
  <div :class="['subscription-card', { 'is-mobile': isMobile }]">
    <div class="subscription-card__header">
      <div class="subscription-card__icon">
        <img v-if="source.sourceIcon" :src="source.sourceIcon" :alt="source.sourceName" />
        <AppstoreOutlined v-else />
      </div>
      <div class="subscription-card__info">
        <div class="subscription-card__name-row">
          <span class="subscription-card__name">{{ source.sourceName }}</span>
          <Tag v-if="source.category" color="cyan" class="subscription-card__category">
            {{ source.category }}
          </Tag>
        </div>
        <div class="subscription-card__meta">
          <span class="subscription-card__count">
            <UserOutlined />
            {{ source.subscriberCount }} 人订阅
          </span>
          <span v-if="source.lastUpdateTime" class="subscription-card__time">
            更新于 {{ source.lastUpdateTime }}
          </span>
        </div>
      </div>
    </div>
    <div class="subscription-card__actions">
      <template v-if="isMobile">
        <Dropdown :trigger="['click']">
          <Button type="text">
            <MoreOutlined />
          </Button>
          <template #overlay>
            <Menu @click="handleMenuClick">
              <Menu.Item v-if="source.status === 'active'" key="pause">
                <PauseCircleOutlined />
                暂停订阅
              </Menu.Item>
              <Menu.Item v-else key="resume">
                <PlayCircleOutlined />
                恢复订阅
              </Menu.Item>
              <Menu.Item key="unsubscribe">
                <CloseCircleOutlined />
                取消订阅
              </Menu.Item>
            </Menu>
          </template>
        </Dropdown>
      </template>
      <template v-else>
        <Button
          v-if="source.status === 'active'"
          type="text"
          @click="handlePause"
        >
          暂停
        </Button>
        <Button
          v-else
          type="text"
          @click="handleResume"
        >
          恢复
        </Button>
        <SubscribeButton
          :user-id="userId"
          :source-id="source.sourceId"
          :source-type="source.sourceType"
          :is-subscribed="true"
          :is-paused="source.status === 'paused'"
          @unsubscribe="handleUnsubscribe"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  AppstoreOutlined,
  UserOutlined,
  MoreOutlined,
  PauseCircleOutlined,
  PlayCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useSubscribeStore } from '/@/store/modules/subscribe';

interface SourceProp {
  sourceId: string;
  sourceName: string;
  sourceIcon: string;
  sourceType: string;
  category: string;
  subscriberCount: number;
  lastUpdateTime: string;
  status: 'active' | 'paused';
}

const props = defineProps<{
  userId: string;
  source: SourceProp;
  isMobile?: boolean;
}>();

const emit = defineEmits<{
  (e: 'subscribe', sourceId: string): void;
  (e: 'unsubscribe', sourceId: string): void;
  (e: 'pause', sourceId: string): void;
  (e: 'resume', sourceId: string): void;
}>();

const subscribeStore = useSubscribeStore();

function handlePause() {
  subscribeStore
    .pause(props.userId, props.source.sourceId)
    .then(() => {
      message.success('已暂停订阅');
      emit('pause', props.source.sourceId);
    })
    .catch(() => {
      message.error('暂停订阅失败');
    });
}

function handleResume() {
  subscribeStore
    .resume(props.userId, props.source.sourceId)
    .then(() => {
      message.success('已恢复订阅');
      emit('resume', props.source.sourceId);
    })
    .catch(() => {
      message.error('恢复订阅失败');
    });
}

function handleUnsubscribe() {
  emit('unsubscribe', props.source.sourceId);
}

function handleMenuClick({ key }: { key: string }) {
  if (key === 'pause') {
    handlePause();
  } else if (key === 'resume') {
    handleResume();
  } else if (key === 'unsubscribe') {
    subscribeStore
      .unsubscribe(props.userId, props.source.sourceId)
      .then(() => {
        message.success('已取消订阅');
        emit('unsubscribe', props.source.sourceId);
      })
      .catch(() => {
        message.error('取消订阅失败');
      });
  }
}
</script>

<style scoped lang="less">
.subscription-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;

  &:hover {
    background-color: #fafafa;
  }

  &__header {
    display: flex;
    align-items: flex-start;
    flex: 1;
    min-width: 0;
  }

  &__icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-right: 12px;
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .anticon {
      font-size: 20px;
      color: #999;
    }
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name-row {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  &__name {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__category {
    flex-shrink: 0;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-top: 4px;
  }

  &__count {
    font-size: 12px;
    color: #666;

    .anticon {
      margin-right: 4px;
    }
  }

  &__time {
    font-size: 12px;
    color: #999;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
    margin-left: 12px;
  }

  &.is-mobile {
    padding: 10px 12px;

    .subscription-card__icon {
      width: 36px;
      height: 36px;
      margin-right: 10px;
    }

    .subscription-card__name {
      font-size: 13px;
    }
  }
}
</style>
