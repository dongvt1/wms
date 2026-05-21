import {knowledgeDeleteAllDoc} from "./AiKnowledgeBase.api";
import {useMessage} from "@/hooks/web/useMessage";

const {createConfirmSync} = useMessage();

// Clear documents
export async function doDeleteAllDoc(knowledgeId: string, reload: () => void) {
  const flag = await createConfirmSync({
    title: 'Clear Documents',
    content: () => (
      <p>
        <span>Are you sure you want to clear all documents?</span>
        <br/>
        <span style="color: #ee0000;">
          This operation will delete all entered documents and cannot be recovered. Please proceed with caution.
        </span>
      </p>
    ),
  });
  if (!flag) {
    return;
  }
  knowledgeDeleteAllDoc(knowledgeId, reload)
}
