<template>
  <div class="tag-manage">
    <h4>标签管理</h4>

    <!-- 新增标签 -->
    <div class="tag-manage__add">
      <a-input-group compact>
        <a-input
          v-model:value="newTagName"
          placeholder="输入标签名称"
          :maxlength="20"
          style="width: calc(100% - 60px)"
          @press-enter="handleAddTag"
        />
        <a-button type="primary" :loading="addLoading" @click="handleAddTag">添加</a-button>
      </a-input-group>
      <div v-if="addError" class="tag-manage__error">{{ addError }}</div>
    </div>

    <!-- 标签列表 -->
    <div class="tag-manage__list">
      <div v-if="tags.length === 0" class="tag-manage__empty">
        暂无标签
      </div>
      <div
        v-for="tag in tags"
        :key="tag.id"
        class="tag-manage__item"
      >
        <!-- 编辑态 -->
        <template v-if="editingTagId === tag.id">
          <a-input
            v-model:value="editingName"
            :maxlength="20"
            size="small"
            style="flex: 1"
            @press-enter="handleSaveEdit(tag)"
            @blur="handleSaveEdit(tag)"
          />
          <a-button size="small" @click="cancelEdit">取消</a-button>
        </template>

        <!-- 展示态 -->
        <template v-else>
          <span
            class="tag-manage__name"
            :class="{ 'tag-manage__name--editable': !isMobile }"
            @click="!isMobile && startEdit(tag)"
          >
            {{ tag.name }}
          </span>
          <span v-if="tag.contentCount !== undefined" class="tag-manage__count">
            {{ tag.contentCount }} 条内容
          </span>
          <div class="tag-manage__actions">
            <a-button v-if="isMobile" type="link" size="small" @click="startEdit(tag)">编辑</a-button>
            <a-popconfirm
              :title="`确认删除标签「${tag.name}」？${tag.contentCount ? `该标签已被 ${tag.contentCount} 条内容使用。` : ''}`"
              @confirm="handleDeleteTag(tag)"
            >
              <a-button type="link" size="small" danger>删除</a-button>
            </a-popconfirm>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { getTagList, createTag, updateTag, deleteTag } from '/@/api/content/channelDiscovery';
import type { ChannelTagVO } from '/@/api/content/model/channelDiscoveryModel';

interface Props {
  channelId: string;
}

const props = defineProps<Props>();

const tags = ref<ChannelTagVO[]>([]);
const newTagName = ref('');
const addLoading = ref(false);
const addError = ref('');
const editingTagId = ref<string | null>(null);
const editingName = ref('');

const isMobile = ref(false);
if (typeof window !== 'undefined') {
  isMobile.value = window.innerWidth < 576;
}

onMounted(() => {
  fetchTags();
});

async function fetchTags() {
  try {
    const data = await getTagList(props.channelId);
    tags.value = data || [];
  } catch {
    // ignore
  }
}

async function handleAddTag() {
  addError.value = '';
  const name = newTagName.value.trim();

  if (!name) {
    addError.value = '标签名称不能为空';
    return;
  }
  if (name.length > 20) {
    addError.value = '标签名称不能超过 20 个字符';
    return;
  }
  if (tags.value.some((t) => t.name === name)) {
    addError.value = '该标签已存在';
    return;
  }

  addLoading.value = true;
  try {
    await createTag({ name, channelId: props.channelId });
    newTagName.value = '';
    message.success('添加成功');
    await fetchTags();
  } catch {
    message.error('添加失败');
  } finally {
    addLoading.value = false;
  }
}

function startEdit(tag: ChannelTagVO) {
  editingTagId.value = tag.id;
  editingName.value = tag.name;
}

function cancelEdit() {
  editingTagId.value = null;
  editingName.value = '';
}

async function handleSaveEdit(tag: ChannelTagVO) {
  const newName = editingName.value.trim();
  if (!newName || newName === tag.name) {
    cancelEdit();
    return;
  }
  if (newName.length > 20) {
    message.warning('标签名称不能超过 20 个字符');
    return;
  }

  try {
    await updateTag({ tagId: tag.id, name: newName });
    message.success('编辑成功');
    cancelEdit();
    await fetchTags();
  } catch {
    message.error('编辑失败');
  }
}

async function handleDeleteTag(tag: ChannelTagVO) {
  try {
    await deleteTag(tag.id);
    message.success('已删除');
    await fetchTags();
  } catch {
    message.error('删除失败');
  }
}
</script>

<style lang="less" scoped>
.tag-manage {
  h4 {
    margin-bottom: 12px;
  }

  &__add {
    margin-bottom: 16px;
  }

  &__error {
    color: #ff4d4f;
    font-size: 12px;
    margin-top: 4px;
  }

  &__list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  &__empty {
    color: #999;
    font-size: 13px;
    text-align: center;
    padding: 16px 0;
  }

  &__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    background: #fafafa;
    border-radius: 6px;
    transition: background 0.2s;

    &:hover {
      background: #f0f0f0;
    }
  }

  &__name {
    font-size: 14px;
    flex: 1;

    &--editable {
      cursor: pointer;
      &:hover {
        color: #1677ff;
      }
    }
  }

  &__count {
    font-size: 12px;
    color: #999;
  }

  &__actions {
    display: flex;
    gap: 4px;
  }
}
</style>
