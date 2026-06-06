import { vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { nextTick } from 'vue';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import StatusCountdown from '/@/components/jeecg/UserStatus/StatusCountdown.vue';

dayjs.extend(duration);

describe('StatusCountdown', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date('2026-06-05T12:00:00Z'));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  const mountCountdown = (props: Record<string, any> = {}) => {
    return mount(StatusCountdown, {
      props: {
        endTime: dayjs().add(1, 'hour').toISOString(),
        ...props,
      },
    });
  };

  it('should render countdown when time remaining', async () => {
    const wrapper = mountCountdown();
    await nextTick();
    expect(wrapper.text()).not.toContain('已到期');
    expect(wrapper.text()).toMatch(/\d+时\d+分\d+秒/);
  });

  it('should show expired text when endTime is in the past', async () => {
    const wrapper = mountCountdown({ endTime: dayjs().subtract(1, 'minute').toISOString() });
    await nextTick();
    expect(wrapper.text()).toContain('已到期');
  });

  it('should call onExpired when countdown reaches zero', async () => {
    const onExpired = vi.fn();
    mountCountdown({
      endTime: dayjs().add(2, 'second').toISOString(),
      onExpired,
    });

    vi.advanceTimersByTime(3000);
    await nextTick();

    expect(onExpired).toHaveBeenCalled();
  });

  it('should update countdown text over time', async () => {
    const wrapper = mountCountdown({
      endTime: dayjs().add(90, 'second').toISOString(),
    });
    await nextTick();

    const initialText = wrapper.text();
    vi.advanceTimersByTime(5000);
    await nextTick();

    expect(wrapper.text()).not.toBe(initialText);
  });

  it('should display minutes and seconds format', async () => {
    const wrapper = mountCountdown({
      endTime: dayjs().add(65, 'second').toISOString(),
    });
    await nextTick();
    expect(wrapper.text()).toMatch(/\d+分\d+秒/);
  });

  it('should display hours format when over 60 minutes', async () => {
    const wrapper = mountCountdown({
      endTime: dayjs().add(2, 'hour').toISOString(),
    });
    await nextTick();
    expect(wrapper.text()).toMatch(/\d+时\d+分\d+秒/);
  });

  it('should display days format when over 24 hours', async () => {
    const wrapper = mountCountdown({
      endTime: dayjs().add(2, 'day').toISOString(),
    });
    await nextTick();
    expect(wrapper.text()).toMatch(/\d+天\d+时\d+分\d+秒/);
  });

  it('should cleanup timer on unmount', () => {
    const clearIntervalSpy = vi.spyOn(global, 'clearInterval');
    const w = mountCountdown();
    w.unmount();
    expect(clearIntervalSpy).toHaveBeenCalled();
  });

  it('should handle visibilitychange event', async () => {
    const wrapper = mountCountdown({
      endTime: dayjs().add(1, 'hour').toISOString(),
    });
    await nextTick();

    Object.defineProperty(document, 'visibilityState', { value: 'visible', writable: true });
    document.dispatchEvent(new Event('visibilitychange'));

    expect(wrapper.find('.status-countdown').exists()).toBe(true);
  });
});
