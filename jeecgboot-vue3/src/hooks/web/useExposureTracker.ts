import { watch, nextTick } from 'vue';
import type { Ref } from 'vue';
import { tryOnMounted, tryOnUnmounted } from '@vueuse/core';

interface ExposureItem {
  id: string;
  type: string;
}

interface ExposureTrackerOptions<T = HTMLElement> {
  containerRef: Ref<T | null>;
  getItems: () => T[];
  onExposure: (items: ExposureItem[]) => void;
  thresholdTime?: number;
  rootMargin?: string;
  trigger?: Ref<unknown>;
}

export function useExposureTracker<T extends HTMLElement = HTMLElement>(options: ExposureTrackerOptions<T>) {
  const { containerRef, getItems, onExposure, thresholdTime = 500, rootMargin = '0px', trigger } = options;

  const exposedSet = new Set<string>();
  const pendingTimers = new Map<Element, ReturnType<typeof setTimeout>>();
  const pendingItems: ExposureItem[] = [];
  let flushTimer: ReturnType<typeof setTimeout> | null = null;
  let observer: IntersectionObserver | null = null;
  let mutationObserver: MutationObserver | null = null;
  let mounted = false;

  function getItemKey(el: Element): string | null {
    const id = el.getAttribute('data-expose-id');
    const type = el.getAttribute('data-expose-type');
    if (!id || !type) return null;
    return `${type}:${id}`;
  }

  function getItemData(el: Element): ExposureItem | null {
    const id = el.getAttribute('data-expose-id');
    const type = el.getAttribute('data-expose-type');
    if (!id || !type) return null;
    return { id, type };
  }

  function flush() {
    if (flushTimer) {
      clearTimeout(flushTimer);
      flushTimer = null;
    }
    if (pendingItems.length > 0) {
      const batch = [...pendingItems];
      pendingItems.length = 0;
      onExposure(batch);
    }
  }

  function scheduleFlush() {
    if (flushTimer !== null) return;
    flushTimer = setTimeout(() => {
      flushTimer = null;
      flush();
    }, 200);
  }

  function queueExposure(el: Element) {
    const key = getItemKey(el);
    if (!key || exposedSet.has(key)) return;

    const existingTimer = pendingTimers.get(el);
    if (existingTimer) {
      clearTimeout(existingTimer);
    }

    const timer = setTimeout(() => {
      pendingTimers.delete(el);
      const k = getItemKey(el);
      const data = getItemData(el);
      if (k && data && !exposedSet.has(k)) {
        exposedSet.add(k);
        pendingItems.push(data);
        scheduleFlush();
      }
    }, thresholdTime);

    pendingTimers.set(el, timer);
  }

  function cancelExposure(el: Element) {
    const timer = pendingTimers.get(el);
    if (timer) {
      clearTimeout(timer);
      pendingTimers.delete(el);
    }
  }

  function flushPending() {
    for (const [el, timer] of pendingTimers) {
      clearTimeout(timer);
      const key = getItemKey(el);
      const data = getItemData(el);
      if (key && data && !exposedSet.has(key)) {
        exposedSet.add(key);
        pendingItems.push(data);
      }
    }
    pendingTimers.clear();
    flush();
  }

  function observeNewItems() {
    if (!observer) return;
    const items = getItems();
    for (const el of items) {
      const key = getItemKey(el as unknown as Element);
      if (key && !exposedSet.has(key)) {
        observer.observe(el as unknown as Element);
      }
    }
  }

  function handleIntersection(entries: IntersectionObserverEntry[]) {
    for (const entry of entries) {
      if (entry.isIntersecting && entry.intersectionRatio > 0) {
        queueExposure(entry.target);
      } else {
        cancelExposure(entry.target);
      }
    }
  }

  function handleBeforeUnload() {
    flushPending();
  }

  function fallbackExposeAll() {
    const items = getItems();
    const toExpose: ExposureItem[] = [];
    for (const el of items) {
      const data = getItemData(el as unknown as Element);
      const key = getItemKey(el as unknown as Element);
      if (data && key && !exposedSet.has(key)) {
        exposedSet.add(key);
        toExpose.push(data);
      }
    }
    if (toExpose.length > 0) {
      pendingItems.push(...toExpose);
      scheduleFlush();
    }
  }

  function createObserver() {
    if (typeof IntersectionObserver === 'undefined') {
      fallbackExposeAll();
      return;
    }

    observer = new IntersectionObserver(handleIntersection, {
      root: containerRef.value,
      rootMargin,
      threshold: [0, 0.1, 0.5, 1],
    });

    observeNewItems();
  }

  function setupMutationObserver() {
    if (typeof MutationObserver === 'undefined' || !containerRef.value) return;
    mutationObserver = new MutationObserver(() => {
      nextTick(() => observeNewItems());
    });
    mutationObserver.observe(containerRef.value, { childList: true, subtree: true });
  }

  function cleanup() {
    if (observer) {
      observer.disconnect();
      observer = null;
    }
    if (mutationObserver) {
      mutationObserver.disconnect();
      mutationObserver = null;
    }
    for (const timer of pendingTimers.values()) {
      clearTimeout(timer);
    }
    pendingTimers.clear();
    if (flushTimer) {
      clearTimeout(flushTimer);
      flushTimer = null;
    }
    if (typeof window !== 'undefined') {
      window.removeEventListener('beforeunload', handleBeforeUnload);
    }
    flushPending();
  }

  function update() {
    if (!mounted) return;
    nextTick(() => observeNewItems());
  }

  tryOnMounted(() => {
    mounted = true;
    createObserver();
    setupMutationObserver();
    if (typeof window !== 'undefined') {
      window.addEventListener('beforeunload', handleBeforeUnload);
    }
  });

  tryOnUnmounted(() => {
    mounted = false;
    cleanup();
  });

  if (trigger) {
    watch(trigger, () => update(), { flush: 'post' });
  }

  return {
    flushPending,
    update,
  };
}
