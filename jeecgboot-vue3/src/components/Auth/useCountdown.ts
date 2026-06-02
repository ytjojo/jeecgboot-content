import { ref, onUnmounted } from 'vue';

export function useCountdown(initial = 60) {
  const seconds = ref(0);
  const active = ref(false);
  let timer: ReturnType<typeof setInterval> | null = null;

  function start(duration = initial) {
    if (active.value) return;
    seconds.value = duration;
    active.value = true;
    timer = setInterval(() => {
      seconds.value -= 1;
      if (seconds.value <= 0) {
        stop();
      }
    }, 1000);
  }

  function stop() {
    active.value = false;
    seconds.value = 0;
    if (timer) {
      clearInterval(timer);
      timer = null;
    }
  }

  onUnmounted(() => stop());

  return { seconds, active, start, stop };
}
