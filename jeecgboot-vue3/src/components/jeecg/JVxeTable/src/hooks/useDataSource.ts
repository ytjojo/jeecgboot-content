import { nextTick, watch } from 'vue';
import { JVxeDataProps, JVxeRefs, JVxeTableMethods } from '../types';
import { cloneDeep, debounce } from 'lodash-es';

export function useDataSource(props, data: JVxeDataProps, methods: JVxeTableMethods, refs: JVxeRefs) {
  // update-begin--author:liaozhiyang---date:20260130---for:【QQYUN-14177】online配置界面，字段配置卡顿
  // 使用浅拷贝优化大数据量处理
  const processDataSource = debounce(async (newDataSource) => {
    if (!Array.isArray(newDataSource)) {
      data.vxeDataSource.value = [];
      return;
    }
    data.vxeDataSource.value = cloneDeep(newDataSource);
    // 批量处理禁用行，减少循环次数
    const disabledRowIds: string[] = [];
    data.vxeDataSource.value.forEach((row, rowIndex) => {
      // 判断是否是禁用行
      if (methods.isDisabledRow(row, rowIndex)) {
        disabledRowIds.push(row.id);
      }
      // 处理联动回显数据
      methods.handleLinkageBackData(row);
    });
    data.disabledRowIds = disabledRowIds;

    const grid = await waitRef(refs.gridRef);
    if (grid?.value) methods.recalcSortNumber();
  }, 50); // 50ms 防抖，避免频繁更新

  watch(
    () => props.dataSource,
    (newDataSource) => {
      processDataSource(newDataSource);
    },
    { immediate: true }
  );
  // update-end--author:liaozhiyang---date:20260130---for:【QQYUN-14177】online配置界面，字段配置卡顿
}
// update-begin--author:liaozhiyang---date:20260130---for:【QQYUN-14177】online配置界面，字段配置卡顿
function waitRef($ref, maxTries = 10) {
  return new Promise<any>((resolve) => {
    let tries = 0;
    (function next() {
      if ($ref.value) {
        resolve($ref);
      } else if (tries >= maxTries) {
        resolve(null);
      } else {
        tries++;
        nextTick(() => next());
      }
    })();
  });
}
// update-end--author:liaozhiyang---date:20260130---for:【QQYUN-14177】online配置界面，字段配置卡顿
