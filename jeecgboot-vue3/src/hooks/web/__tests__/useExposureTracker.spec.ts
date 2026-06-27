import { ref } from 'vue';
import { useExposureTracker } from '/@/hooks/web/useExposureTracker';

const mockSendBeacon = vi.fn();
Object.defineProperty(navigator, 'sendBeacon', { value: mockSendBeacon, writable: true, configurable: true });

let mockObserverInstances: MockIntersectionObserver[] = [];

class MockIntersectionObserver {
  callback: IntersectionObserverCallback;
  elements: Element[] = [];
  rootMargin: string;
  threshold: number | number[];

  constructor(cb: IntersectionObserverCallback, options?: IntersectionObserverInit) {
    this.callback = cb;
    this.rootMargin = options?.rootMargin ?? '0px';
    this.threshold = options?.threshold ?? 0;
    mockObserverInstances.push(this);
  }
  observe(el: Element) {
    if (!this.elements.includes(el)) {
      this.elements.push(el);
    }
  }
  unobserve(el: Element) {
    this.elements = this.elements.filter(e => e !== el);
  }
  disconnect() {
    this.elements = [];
  }
  trigger(entries: { target: Element; isIntersecting: boolean; intersectionRatio: number }[]) {
    this.callback(entries as IntersectionObserverEntry[], this as unknown as IntersectionObserver);
  }
}

describe('useExposureTracker', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.useFakeTimers();
    mockObserverInstances = [];
    (global as any).IntersectionObserver = MockIntersectionObserver;
    delete (window as any).onbeforeunload;
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  function createTestItem(id: string, type = 'CIRCLE') {
    const el = document.createElement('div');
    el.dataset.exposeId = id;
    el.dataset.exposeType = type;
    return el;
  }

  function setup() {
    const onExposure = vi.fn();
    const containerRef = ref<HTMLElement | null>(document.createElement('div'));
    const items: HTMLElement[] = [];
    const itemsRef = ref<HTMLElement[]>(items);

    const item1 = createTestItem('s1');
    const item2 = createTestItem('s2');
    containerRef.value!.appendChild(item1);
    containerRef.value!.appendChild(item2);
    items.push(item1, item2);

    return { onExposure, containerRef, itemsRef, item1, item2, items };
  }

  function advanceTimersAndFlush(ms: number) {
    vi.advanceTimersByTime(ms);
    vi.advanceTimersByTime(200);
  }

  it('should track exposed items via callback when threshold time is met', () => {
    const { onExposure, containerRef, itemsRef, item1, item2 } = setup();

    useExposureTracker({
      containerRef,
      getItems: () => itemsRef.value,
      onExposure,
      thresholdTime: 500,
    });

    expect(mockObserverInstances.length).toBe(1);
    const observer = mockObserverInstances[0];

    observer.trigger([{ target: item1, isIntersecting: true, intersectionRatio: 1 }]);

    expect(onExposure).not.toHaveBeenCalled();

    advanceTimersAndFlush(500);
    expect(onExposure).toHaveBeenCalledTimes(1);
    expect(onExposure).toHaveBeenCalledWith([{ id: 's1', type: 'CIRCLE' }]);

    observer.trigger([{ target: item2, isIntersecting: true, intersectionRatio: 1 }]);
    advanceTimersAndFlush(500);
    expect(onExposure).toHaveBeenCalledTimes(2);
    expect(onExposure).toHaveBeenLastCalledWith([{ id: 's2', type: 'CIRCLE' }]);
  });

  it('should not track already-exposed items twice', () => {
    const { onExposure, containerRef, itemsRef, item1 } = setup();

    useExposureTracker({
      containerRef,
      getItems: () => itemsRef.value,
      onExposure,
      thresholdTime: 500,
    });

    const observer = mockObserverInstances[0];

    observer.trigger([{ target: item1, isIntersecting: true, intersectionRatio: 1 }]);
    advanceTimersAndFlush(500);
    expect(onExposure).toHaveBeenCalledTimes(1);

    observer.trigger([{ target: item1, isIntersecting: false, intersectionRatio: 0 }]);
    observer.trigger([{ target: item1, isIntersecting: true, intersectionRatio: 1 }]);
    advanceTimersAndFlush(500);
    expect(onExposure).toHaveBeenCalledTimes(1);
  });

  it('should cancel exposure tracking if element leaves viewport before threshold', () => {
    const { onExposure, containerRef, itemsRef, item1 } = setup();

    useExposureTracker({
      containerRef,
      getItems: () => itemsRef.value,
      onExposure,
      thresholdTime: 500,
    });

    const observer = mockObserverInstances[0];

    observer.trigger([{ target: item1, isIntersecting: true, intersectionRatio: 1 }]);
    vi.advanceTimersByTime(300);
    observer.trigger([{ target: item1, isIntersecting: false, intersectionRatio: 0 }]);
    vi.advanceTimersByTime(700);

    expect(onExposure).not.toHaveBeenCalled();
  });

  it('should batch exposure reports with debounce', () => {
    const { onExposure, containerRef, itemsRef, item1, item2 } = setup();

    useExposureTracker({
      containerRef,
      getItems: () => itemsRef.value,
      onExposure,
      thresholdTime: 500,
    });

    const observer = mockObserverInstances[0];

    observer.trigger([{ target: item1, isIntersecting: true, intersectionRatio: 1 }]);
    observer.trigger([{ target: item2, isIntersecting: true, intersectionRatio: 1 }]);

    vi.advanceTimersByTime(500);
    expect(onExposure).not.toHaveBeenCalled();

    vi.advanceTimersByTime(200);
    expect(onExposure).toHaveBeenCalledTimes(1);
    expect(onExposure).toHaveBeenCalledWith([
      { id: 's1', type: 'CIRCLE' },
      { id: 's2', type: 'CIRCLE' },
    ]);
  });

  it('should not track if IntersectionObserver is not supported', () => {
    delete (global as any).IntersectionObserver;

    const { onExposure, containerRef, itemsRef } = setup();

    expect(() => {
      useExposureTracker({
        containerRef,
        getItems: () => itemsRef.value,
        onExposure,
        thresholdTime: 500,
      });
    }).not.toThrow();
  });

  it('should fallback to immediate exposure when IntersectionObserver is not available', () => {
    delete (global as any).IntersectionObserver;

    const { onExposure, containerRef, itemsRef } = setup();

    useExposureTracker({
      containerRef,
      getItems: () => itemsRef.value,
      onExposure,
      thresholdTime: 500,
    });

    vi.advanceTimersByTime(200);
    expect(onExposure).toHaveBeenCalledWith([
      { id: 's1', type: 'CIRCLE' },
      { id: 's2', type: 'CIRCLE' },
    ]);
  });
});
