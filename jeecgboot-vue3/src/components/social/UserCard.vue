<template>
  <div :class="['user-card', { 'is-mobile': isMobile }]">
    <div class="user-card__header">
      <div class="user-card__avatar-wrapper" @click="handleAvatarClick">
        <Avatar :size="isMobile ? 40 : 48" :src="user.avatar" />
        <div v-if="user.isSpecial" class="user-card__special-badge">
          <StarFilled />
        </div>
      </div>
      <div class="user-card__info">
        <div class="user-card__name-row">
          <span class="user-card__nickname">{{ user.nickname }}</span>
          <Tag v-if="user.groupName" color="blue" class="user-card__group-tag">
            {{ user.groupName }}
          </Tag>
        </div>
        <div v-if="!isMobile && user.bio" class="user-card__bio">{{ user.bio }}</div>
        <div v-if="!isMobile && user.followTime" class="user-card__time">
          关注时间：{{ user.followTime }}
        </div>
      </div>
    </div>
    <div class="user-card__actions">
      <template v-if="isMobile">
        <Dropdown :trigger="['click']">
          <Button type="text">
            <MoreOutlined />
          </Button>
          <template #overlay>
            <Menu @click="handleMenuClick">
              <Menu.Item key="group">
                <TeamOutlined />
                调整分组
              </Menu.Item>
              <Menu.Item key="special">
                <StarFilled v-if="user.isSpecial" style="color: #faad14" />
                <StarOutlined v-else />
                {{ user.isSpecial ? '取消特别关注' : '设为特别关注' }}
              </Menu.Item>
              <Menu.Item key="unfollow">
                <UserDeleteOutlined />
                取消关注
              </Menu.Item>
            </Menu>
          </template>
        </Dropdown>
      </template>
      <template v-else>
        <SpecialFollowButton
          :user-id="userId"
          :target-user-id="user.userId"
          :is-following="true"
          :is-special="user.isSpecial"
          @special="() => handleSpecialChange(true)"
          @cancel-special="() => handleSpecialChange(false)"
        />
        <FollowButton
          :user-id="userId"
          :target-user-id="user.userId"
          :is-following="true"
          @unfollow="handleUnfollow"
        />
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { StarFilled, StarOutlined, MoreOutlined, UserDeleteOutlined, TeamOutlined } from '@ant-design/icons-vue';
import { message } from 'ant-design-vue';
import { useFollowStore } from '/@/store/modules/follow';

interface UserProp {
  userId: string;
  nickname: string;
  avatar: string;
  bio?: string;
  followTime?: string;
  groupName?: string;
  isSpecial?: boolean;
  lastActiveTime?: string;
}

const props = defineProps<{
  userId: string;
  user: UserProp;
  isMobile?: boolean;
}>();

const emit = defineEmits<{
  (e: 'unfollow', userId: string): void;
  (e: 'specialChange', userId: string, isSpecial: boolean): void;
  (e: 'groupChange', userId: string): void;
}>();

const followStore = useFollowStore();

function handleAvatarClick() {
  window.open(`/user/${props.user.userId}`, '_blank', 'noopener,noreferrer');
}

function handleUnfollow() {
  emit('unfollow', props.user.userId);
}

function handleSpecialChange(isSpecial: boolean) {
  emit('specialChange', props.user.userId, isSpecial);
}

function handleMenuClick({ key }: { key: string }) {
  if (key === 'special') {
    const next = !props.user.isSpecial;
    const storeMethod = next
      ? followStore.setSpecial(props.userId, props.user.userId)
      : followStore.cancelSpecial(props.userId, props.user.userId);

    storeMethod
      .then(() => {
        message.success(next ? '已设为特别关注' : '已取消特别关注');
        emit('specialChange', props.user.userId, next);
      })
      .catch(() => {
        message.error(next ? '设置特别关注失败' : '取消特别关注失败');
      });
  } else if (key === 'group') {
    emit('groupChange', props.user.userId);
  } else if (key === 'unfollow') {
    followStore
      .unfollow(props.userId, props.user.userId)
      .then(() => {
        message.success('已取消关注');
        emit('unfollow', props.user.userId);
      })
      .catch(() => {
        message.error('取消关注失败');
      });
  }
}
</script>

<style scoped lang="less">
.user-card {
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

  &__avatar-wrapper {
    position: relative;
    flex-shrink: 0;
    cursor: pointer;
    margin-right: 12px;
  }

  &__special-badge {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 16px;
    height: 16px;
    border-radius: 50%;
    background: #faad14;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 10px;
    color: #fff;
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

  &__nickname {
    font-size: 14px;
    font-weight: 500;
    color: #1a1a1a;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__group-tag {
    flex-shrink: 0;
  }

  &__bio {
    font-size: 12px;
    color: #666;
    margin-top: 4px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__time {
    font-size: 12px;
    color: #999;
    margin-top: 2px;
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

    .user-card__avatar-wrapper {
      margin-right: 10px;
    }

    .user-card__nickname {
      font-size: 13px;
    }
  }
}
</style>
