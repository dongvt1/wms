import { h } from 'vue';
import JVxeToolbar from '../components/JVxeToolbar.vue';
import { JVxeDataProps, JVxeTableMethods, JVxeTableProps } from '../types';

export function useToolbar(props: JVxeTableProps, data: JVxeDataProps, methods: JVxeTableMethods, $slots) {
  /** Render toolbar */
  function renderToolbar() {
    if (props.toolbar) {
      return h(
        JVxeToolbar,
        {
          size: props.size,
          disabled: props.disabled,
          toolbarConfig: props.toolbarConfig,
          disabledRows: props.disabledRows,
          hasBtnAuth: methods.hasBtnAuth,
          selectedRowIds: data.selectedRowIds.value,
          custom: props.custom,
          addBtnCfg: props.addBtnCfg,
          removeBtnCfg: props.removeBtnCfg,
          // Add event
          onAdd: () => {
            // update-begin--author:liaozhiyang---date:20240521---for：【TV360X-212】onlineA verification prompt appears when a new field is added.
            setTimeout(() => {
              methods.addRows();
            }, 0);
            // update-end--author:liaozhiyang---date:20240521---for：【TV360X-212】onlineA verification prompt appears when a new field is added.
          },
          // save event
          onSave: () => methods.trigger('save'),
          onRemove() {
            const $table = methods.getXTable();
            // update-begin--author:liaozhiyang---date:20231018---for：【QQYUN-6805】repairasyncRemoveField is not valid
            // trigger delete event
            if (data.selectedRows.value.length > 0) {
              const deleteOldRows = methods.filterNewRows(data.selectedRows.value);
              const removeEvent: any = { deleteRows: data.selectedRows.value, $table };
              const insertRecords = $table.getInsertRecords();
              if (props.asyncRemove && deleteOldRows.length) {
                data.selectedRows.value.forEach((item) => {
                  // Delete newly added dataid
                  if (insertRecords.includes(item)) {
                    delete item.id;
                  }
                });
                // Confirm deletion，Only by calling this method will it be deleted.
                removeEvent.confirmRemove = () => methods.removeSelection();
              } else {
                if (props.asyncRemove) {
                  // asyncRemoveWhen only newly added data is deleted，prevent callingconfirmRemoveReport an error
                  removeEvent.confirmRemove = () => {};
                }
                methods.removeSelection();
              }
              methods.trigger('removed', removeEvent);
            } else {
              methods.removeSelection();
            }
            // update-end--author:liaozhiyang---date:20231018---for：【QQYUN-6805】repairasyncRemoveField is not valid
          },
          // Clear selection event
          onClearSelection: () => methods.clearSelection(),
          onRegister: ({ xToolbarRef }) => methods.getXTable().connect(xToolbarRef.value),
        },
        {
          toolbarPrefix: $slots.toolbarPrefix,
          toolbarSuffix: $slots.toolbarSuffix,
        }
      );
    }
    return null;
  }

  return { renderToolbar };
}
