<template>
  <div class="help-article-page">
    <a-card>
      <a-breadcrumb>
        <a-breadcrumb-item @click="router.push('/support/help')">帮助中心</a-breadcrumb-item>
        <a-breadcrumb-item>{{ article?.categoryName }}</a-breadcrumb-item>
        <a-breadcrumb-item>{{ article?.title }}</a-breadcrumb-item>
      </a-breadcrumb>

      <div v-if="loading" class="loading">
        <a-spin />
      </div>

      <template v-else-if="article">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span>浏览 {{ article.viewCount }}</span>
          <span>有帮助 {{ article.helpfulCount }}</span>
        </div>
        <div class="article-content" v-html="article.content" />

        <a-divider />
        <ArticleFeedback :article-id="articleId" @feedback="handleFeedback" />
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { getHelpArticleDetail, type HelpArticle } from '/@/api/support/help';
import ArticleFeedback from './components/ArticleFeedback.vue';

const router = useRouter();
const route = useRoute();
const articleId = route.params.id as string;
const article = ref<HelpArticle | null>(null);
const loading = ref(true);

const handleFeedback = (helpful: boolean) => {
  if (article.value) {
    if (helpful) article.value.helpfulCount++;
    else article.value.unhelpfulCount++;
  }
};

onMounted(async () => {
  try {
    const res = await getHelpArticleDetail(articleId);
    article.value = res.result;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped lang="less">
.help-article-page {
  padding: 16px;

  .article-title {
    margin-top: 24px;
    font-size: 24px;
  }

  .article-meta {
    color: #999;
    margin: 12px 0;

    span {
      margin-right: 16px;
    }
  }

  .article-content {
    line-height: 1.8;
    font-size: 15px;

    img {
      max-width: 100%;
      cursor: pointer;
    }
  }

  .loading {
    text-align: center;
    padding: 48px;
  }
}
</style>
