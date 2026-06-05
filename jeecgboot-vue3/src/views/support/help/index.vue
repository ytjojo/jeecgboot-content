<template>
  <div class="help-center-page">
    <a-card>
      <h2>帮助中心</h2>
      <HelpSearch @search="handleSearch" />

      <div v-if="!searchResults" class="categories-section">
        <a-row :gutter="[16, 16]">
          <a-col v-for="category in categories" :key="category.id" :xs="12" :sm="8" :md="6">
            <a-card hoverable @click="goToCategory(category)">
              <template #cover>
                <div class="category-icon">
                  <component :is="getCategoryIcon(category.icon)" style="font-size: 32px" />
                </div>
              </template>
              <a-card-meta :title="category.name">
                <template #description>{{ category.articleCount }} 篇文章</template>
              </a-card-meta>
            </a-card>
          </a-col>
        </a-row>
      </div>

      <div v-else class="search-results">
        <a-list :data-source="searchResults" :loading="searching">
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta>
                <template #title>
                  <a @click="goToArticle(item.id)">{{ item.title }}</a>
                </template>
                <template #description>{{ item.summary }}</template>
              </a-list-item-meta>
            </a-list-item>
          </template>
        </a-list>
        <a-empty v-if="!searching && searchResults.length === 0" description="未找到相关文章">
          <a-button type="primary" @click="goToCustomerService">联系客服</a-button>
        </a-empty>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { QuestionCircleOutlined, SafetyOutlined, AlertOutlined, TrophyOutlined, MoreOutlined } from '@ant-design/icons-vue';
import { getHelpCategories, searchHelpArticles, type HelpCategory, type HelpSearchResult } from '/@/api/support/help';
import HelpSearch from './components/HelpSearch.vue';

const router = useRouter();
const categories = ref<HelpCategory[]>([]);
const searchResults = ref<HelpSearchResult[] | null>(null);
const searching = ref(false);

const iconMap: Record<string, any> = {
  question: QuestionCircleOutlined,
  safety: SafetyOutlined,
  alert: AlertOutlined,
  trophy: TrophyOutlined,
  more: MoreOutlined,
};

const getCategoryIcon = (icon: string) => iconMap[icon] || QuestionCircleOutlined;

const handleSearch = async (keyword: string) => {
  searching.value = true;
  try {
    const res = await searchHelpArticles(keyword);
    searchResults.value = res.result || [];
  } finally {
    searching.value = false;
  }
};

const goToCategory = (category: HelpCategory) => {
  router.push({ path: '/support/help/category', query: { id: category.id, name: category.name } });
};

const goToArticle = (id: string) => {
  router.push({ path: `/support/help/article/${id}` });
};

const goToCustomerService = () => {
  router.push('/support/customer-service');
};

onMounted(async () => {
  const res = await getHelpCategories();
  categories.value = res.result || [];
});
</script>

<style scoped lang="less">
.help-center-page {
  padding: 16px;

  .categories-section {
    margin-top: 24px;
  }

  .category-icon {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 80px;
    background: #f5f5f5;
  }

  .search-results {
    margin-top: 24px;
  }
}
</style>
