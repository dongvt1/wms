//package org.jeecg.modules.airag.test;
//
//import dev.langchain4j.data.document.Document;
//import dev.langchain4j.data.document.DocumentSplitter;
//import dev.langchain4j.data.document.Metadata;
//import dev.langchain4j.data.document.splitter.DocumentSplitters;
//import dev.langchain4j.data.embedding.Embedding;
//import dev.langchain4j.data.segment.TextSegment;
//import dev.langchain4j.memory.ChatMemory;
//import dev.langchain4j.memory.chat.MessageWindowChatMemory;
//import dev.langchain4j.model.chat.ChatLanguageModel;
//import dev.langchain4j.model.embedding.EmbeddingModel;
//import dev.langchain4j.model.input.PromptTemplate;
//import dev.langchain4j.model.openai.OpenAiTokenizer;
//import dev.langchain4j.rag.DefaultRetrievalAugmentor;
//import dev.langchain4j.rag.RetrievalAugmentor;
//import dev.langchain4j.rag.content.injector.DefaultContentInjector;
//import dev.langchain4j.rag.content.retriever.ContentRetriever;
//import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
//import dev.langchain4j.rag.query.router.DefaultQueryRouter;
//import dev.langchain4j.rag.query.router.QueryRouter;
//import dev.langchain4j.service.AiServices;
//import dev.langchain4j.store.embedding.EmbeddingMatch;
//import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
//import dev.langchain4j.store.embedding.EmbeddingStore;
//import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
//import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
//import lombok.extern.slf4j.Slf4j;
//import org.jeecg.ai.assistant.AiChatAssistant;
//import org.jeecg.ai.factory.AiModelFactory;
//import org.jeecg.ai.factory.AiModelOptions;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.List;
//
//import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;
//
///**
// * @Description: Process testing
// * @Author: chenrui
// * @Date: 2025/2/11 16:11
// */
//@Slf4j
//public class TestVector {
//
//    String openAIBaseUrl = "https://api.v3.cm/v1";
//    String openAIApiKey = "sk-xxx";
//
//    EmbeddingModel embeddingModel;
//    EmbeddingStore<TextSegment> embeddingStore;
//
//    @Before
//    public void before() {
//        AiModelOptions.AiModelOptionsBuilder modelOpBuilder = AiModelOptions.builder().provider(AiModelFactory.AIMODEL_TYPE_OPENAI)
//                .baseUrl(openAIBaseUrl).apiKey(openAIApiKey);
//        embeddingModel = AiModelFactory.createEmbeddingModel(modelOpBuilder.build());
//        embeddingStore = PgVectorEmbeddingStore.builder()
//                // Connection and table parameters
//                .host("localhost")
//                .port(15432)
//                .database("postgres")
//                .user("postgres")
//                .password("123456")
//                .table("test_embeddings")
//                // Embedding dimension
//                .dimension(embeddingModel.dimension())      // Required: Must match the embedding model’s output dimension
//                // Indexing and performance options
//                .useIndex(true)                             // Enable IVFFlat index
//                .indexListSize(100)                         // Number of lists for IVFFlat index
//                // Table creation options
//                .createTable(true)                          // Automatically create the table if it doesn’t exist
//                .dropTableFirst(false)                      // Don’t drop the table first (set to true if you want a fresh start)
//                .build();
//    }
//
//    @Test
//    public void testSave2Vector() {
//        embeddingStore.removeAll(metadataKey("id").isEqualTo("sdfsdf"));
//        DocumentSplitter splitter = DocumentSplitters.recursive(200,
//                50,
//                new OpenAiTokenizer());
//        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
//                .documentSplitter(splitter)
//                .embeddingModel(embeddingModel)
//                .embeddingStore(embeddingStore)
//                .build();
//        Document from = Document.from(doc, Metadata.metadata("id", "sdfsdf"));
//        ingestor.ingest(from);
//    }
//
//    @Test
//    public void testQueryByVector() {
//        Embedding queryEmbedding = embeddingModel.embed("What is the difference between full-time work and part-time work??").content();
//        EmbeddingSearchRequest embeddingSearchRequest = EmbeddingSearchRequest.builder()
//                .queryEmbedding(queryEmbedding)
//                .maxResults(5)
//                .filter(metadataKey("id").isEqualTo("sdfsdf"))
//                .build();
//
//        List<EmbeddingMatch<TextSegment>> relevant = embeddingStore.search(embeddingSearchRequest).matches();
//        for (int i = 0; i < relevant.size(); i++) {
//            EmbeddingMatch<TextSegment> embeddingMatch = relevant.get(i);
//            System.out.println("result:" + i + "=================================================");
//            System.out.println("Fraction:" + embeddingMatch.score()); // 0.8144288608390052
//            System.out.println("content:" + embeddingMatch.embedded().text()); // I like football.
//        }
//    }
//
//
//    @Test
//    public void testQueryByRAG() {
//        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
//                .embeddingStore(embeddingStore)
//                .embeddingModel(embeddingModel)
//                .maxResults(5)
//                // maxResults can also be specified dynamically depending on the query
////                .dynamicMaxResults(query -> 3)
//                .minScore(0.75)
//                // minScore can also be specified dynamically depending on the query
////                .dynamicMinScore(query -> 0.75)
//                .filter(metadataKey("id").isEqualTo("sdfsdf"))
//                // filter can also be specified dynamically depending on the query
////                .dynamicFilter(query -> {
////                    String userId = getUserId(query.metadata().chatMemoryId());
////                    return metadataKey("userId").isEqualTo(userId);
////                })
//                .build();
//
//        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);
//        ChatLanguageModel chatModel = AiModelFactory.createChatModel(AiModelOptions.builder()
//                .baseUrl(openAIBaseUrl).apiKey(openAIApiKey).provider(AiModelFactory.AIMODEL_TYPE_OPENAI).build());
//
//        QueryRouter queryRouter = new DefaultQueryRouter(contentRetriever);
//        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder().queryRouter(queryRouter)
//                .contentInjector(DefaultContentInjector.builder()
//                        .promptTemplate(PromptTemplate.from("{{userMessage}}\n\nUse the following information to answer the questions:\n{{contents}}\n\n"))
//                        .build()).build();
//        AiChatAssistant assistant = AiServices.builder(AiChatAssistant.class)
//                .chatLanguageModel(chatModel)
//                .chatMemory(chatMemory)
//                .retrievalAugmentor(retrievalAugmentor)
//                .build();
//        String chat = assistant.chat("No labor contract signed?");
//        System.out.println(chat);
//
//    }
//
//
//    String doc = "Labor Contract Law of the People's Republic of China\n" +
//            "Already based on2013.07.01Implemented amendment changes\n" +
//            "Table of contents\n" +
//            "Chapter 1　General principles\n" +
//            "Chapter 2　Conclusion of labor contract\n" +
//            "Chapter 3　Performance and modification of labor contracts\n" +
//            "Chapter 4　Cancellation and termination of labor contract\n" +
//            "Chapter 5　Special provisions\n" +
//            "Chapter 6　Supervision and inspection\n" +
//            "Chapter 7　legal liability\n" +
//            "Chapter 8　Supplementary Provisions\n" +
//            "Chapter 1　General principles\n" +
//            "Article 1　In order to improve the labor contract system，Clarify the rights and obligations of both parties to the labor contract，Protect the legitimate rights and interests of workers，Build and develop harmonious and stable labor relations，enact this law。\n" +
//            "Article 2　Enterprises within the People's Republic of China、individual economic organization、Private non-enterprise units and other organizations(Hereinafter referred to as the employer)Establish labor relations with workers，enter into、fulfill、change、Cancellation or termination of labor contract，Apply this law。\n" +
//            "State agencies、Public institutions、Social groups and workers with whom they have established labor relations，enter into、fulfill、change、Cancellation or termination of labor contract，Implemented in accordance with this law。\n" +
//            "Article 3　enter into劳动合同，should follow legal、fair、Equality and voluntariness、consensus、The principle of good faith。\n" +
//            "依法enter into的劳动合同具有约束力，用人单位与劳动者应当fulfill劳动合同约定的义务。\n" +
//            "Article 4　Employers should establish and improve labor regulations and systems in accordance with the law，Ensure workers enjoy labor rights、fulfill劳动义务。\n" +
//            "The employer is formulating、Modify or decide on labor remuneration、working hours、rest vacation、Labor safety and health、insurance benefits、staff training、When labor discipline, labor quota management and other rules and regulations or major matters directly related to the vital interests of workers are，It should be discussed by the workers’ congress or all workers，Put forward plans and opinions，Determine through equal consultation with the trade union or employee representatives。\n" +
//            "In the process of implementing rules and regulations and decisions on major matters，The trade union or employees consider it inappropriate，Have the right to submit to the employer，Modify and improve through negotiation。\n" +
//            "Employers should make public decisions on rules, regulations and major matters that directly affect the vital interests of workers.，Or inform workers。\n" +
//            "Article 5　The labor administrative departments of the people's governments at or above the county level, together with representatives from trade unions and enterprises,，Establish and improve the tripartite mechanism for coordinating labor relations，Jointly study and resolve major issues related to labor relations。\n" +
//            "Article 6　Unions should help、指导劳动者与用人单位依法enter into和fulfill劳动合同，and establish a collective consultation mechanism with employers，Safeguard the legitimate rights and interests of workers。\n" +
//            "Chapter 2　Conclusion of labor contract\n" +
//            "Article 7　用人单位自用工之日起即Establish labor relations with workers。The employer shall establish a roster of employees for reference。\n" +
//            "Article 8　When an employer recruits workers，应当如实告知劳动者工作content、working conditions、work place、occupational hazards、Safety production status、labor remuneration，and other information that workers require to know;The employer has the right to know the basic information of the employee directly related to the labor contract，Workers should truthfully explain。\n" +
//            "Article 9　Employers recruit workers，Workers’ resident identity cards and other documents shall not be detained，Workers shall not be required to provide guarantees or collect property from workers in other names.。\n" +
//            "Article 10　Establish labor relations，应当enter into书面劳动合同。\n" +
//            "已Establish labor relations，未同时enter into书面劳动合同的，应当自用工之日起one个moon内enter into书面劳动合同。\n" +
//            "用人单位与劳动者在用工前enter into劳动合同的，Labor relations are established from the date of employment。\n" +
//            "Article 11　用人单位未在用工的同时enter into书面劳动合同，与劳动者约定的labor remuneration不明确的，新招用的劳动者的labor remuneration按照collective contract规定的标准执行;There is no collective contract or the collective contract does not provide for，Implement equal pay for equal work。\n" +
//            "Article 12　Labor contracts are divided into fixed-term labor contracts、Labor contracts with no fixed term and labor contracts with a term based on the completion of certain work tasks。\n" +
//            "Article 13　Fixed term labor contract，It refers to a labor contract in which the employer and the employee agree on the time for the termination of the contract.。\n" +
//            "用人单位与劳动者consensus，可以enter intoFixed term labor contract。\n" +
//            "Article 14　无Fixed term labor contract，It refers to a labor contract in which the employer and the employee agree that there is no fixed termination time.。\n" +
//            "用人单位与劳动者consensus，可以enter into无Fixed term labor contract。One of the following situations，The employee proposes or agrees to renew the contract、enter into劳动合同的，除劳动者提出enter intoFixed term labor contract外，应当enter into无Fixed term labor contract：\n" +
//            "(one)The employee has worked continuously for ten years in the employer;\n" +
//            "(two)用人单位初次实行劳动合同制度或者国有企业改制重新enter into劳动合同时，The employee has worked continuously for the employer for ten years and is less than ten years away from the statutory retirement age.;\n" +
//            "(three)连续enter intotwo次Fixed term labor contract，且劳动者没有本法第three十Nine条和第Four十条第one项、第two项规定的情形，Renewal of labor contract。\n" +
//            "用人单位自用工之日起满oneYear不与劳动者enter into书面劳动合同的，视为用人单位与劳动者已enter into无Fixed term labor contract。\n" +
//            "Article 15　以完成one定工作任务为期限的劳动合同，It refers to a labor contract in which the employer and the employee agree that the completion of a certain job is the contract term.。\n" +
//            "用人单位与劳动者consensus，可以enter into以完成one定工作任务为期限的劳动合同。\n" +
//            "Article 16　劳动合同由用人单位与劳动者consensus，It shall take effect after the employer and the employee sign or stamp the labor contract text.。\n" +
//            "劳动合同文本由用人单位和劳动者各执one份。\n" +
//            "Article 17　The labor contract should have the following clauses：\n" +
//            "(one)Name of employer、Domicile and legal representative or principal responsible person;\n" +
//            "(two)worker's name、Address and resident ID card or other valid ID number;\n" +
//            "(three)Labor contract term;\n" +
//            "(Four)工作content和work place;\n" +
//            "(five)working hours和rest vacation;\n" +
//            "(six)labor remuneration;\n" +
//            "(seven)social insurance;\n" +
//            "(eight)labor protection、劳动条件和occupational hazards防护;\n" +
//            "(Nine)law、Other matters that should be included in the labor contract as stipulated by regulations。\n" +
//            "In addition to the necessary terms stipulated in the preceding paragraph, the labor contract，The employer and the employee can agree on a probation period、training、Keep a secret、Supplementary insurance and welfare benefits and other matters。\n" +
//            "第十eight条　劳动合同对labor remuneration和劳动条件等标准约定不明确，controversial，Employers and workers can renegotiate;Negotiable，Collective contract provisions apply;没有collective contract或者collective contract未规定labor remuneration的，Implement equal pay for equal work;There is no collective contract or the collective contract does not stipulate labor conditions and other standards.，Applicable national regulations。\n" +
//            "第十Nine条　Labor contract termthree个moon以上不满oneYear的，试用期不得超过one个moon;Labor contract termoneYear以上不满threeYear的，试用期不得超过two个moon;threeYear以上固定期限和无固定期限的劳动合同，试用期不得超过six个moon。\n" +
//            "同one用人单位与同one劳动者只能约定one次试用期。\n" +
//            "以完成one定工作任务为期限的劳动合同或者Labor contract term不满three个moon的，No probation period is allowed。\n" +
//            "试用期包含在Labor contract term内。The labor contract only stipulates a probation period，The probation period is not established，该期限为Labor contract term。\n" +
//            "第two十条　劳动者在试用期的工资不得低于本单位相同岗位最低档工资或者劳动合同约定工资的百分之eight十，and shall not be lower than the minimum wage standard in the location of the employer。\n" +
//            "第two十one条　during trial period，除劳动者有本法第three十Nine条和第Four十条第one项、第two项规定的情形外，The employer shall not terminate the labor contract。The employer terminates the labor contract during the probation period，The reasons should be explained to workers。\n" +
//            "第two十two条　用人单位为劳动者提供专项training费用，对其进行专业技术training的，可以与该劳动者enter into协议，Agreed service period。\n" +
//            "The employee violates the service period agreement，Liquidated damages shall be paid to the employer as agreed。违约金的数额不得超过用人单位提供的training费用。用人单位要求劳动者支付的违约金不得超过服务期尚未fulfill部分所应分摊的training费用。\n" +
//            "用人单位与劳动者Agreed service period的，不影响按照正常的工资调整机制提高劳动者在服务期期间的labor remuneration。\n" +
//            "第two十three条　The employer and the employee can agree in the labor contract to keep the employer's business secrets and confidentiality matters related to intellectual property rights.。\n" +
//            "Workers with confidentiality obligations，Employers can agree on non-competition clauses with employees in labor contracts or confidentiality agreements.，并约定在Cancellation or termination of labor contract后，Provide workers with monthly financial compensation during the non-competition period。The employee violates the non-competition agreement，Liquidated damages shall be paid to the employer as agreed。\n" +
//            "第two十Four条　Persons subject to non-competition restrictions are limited to senior managers of the employer、Senior technical personnel and other personnel with confidentiality obligations。Scope of non-compete restrictions、area、The time limit is agreed between the employer and the employee，竞业限制的约定不得违反law、regulations。\n" +
//            "在Cancellation or termination of labor contract后，The personnel specified in the preceding paragraph are required to produce or operate similar products to the unit、Other competing employers engaged in similar businesses，Or start your own business to produce or operate similar products、Non-competition period for similar businesses，不得超过twoYear。\n" +
//            "第two十five条　除本法第two十two条和第two十three条规定的情形外，The employer shall not agree with the employee that the employee shall bear liquidated damages。\n" +
//            "第two十six条　The following labor contracts are invalid or partially invalid：\n" +
//            "(one)to defraud、Coercive means or taking advantage of others' danger，使对方在违背真实意思的情况下enter into或者change劳动合同的;\n" +
//            "(two)The employer exempts itself from statutory responsibilities、excluding workers’ rights;\n" +
//            "(three)违反law、Mandatory provisions of administrative regulations。\n" +
//            "Dispute over the invalidity or partial invalidity of the labor contract，Confirmed by labor dispute arbitration institution or people's court。\n" +
//            "第two十seven条　Part of the labor contract is invalid，Does not affect the effectiveness of other parts，Other parts are still valid。\n" +
//            "第two十eight条　The labor contract was confirmed to be invalid，workers have paid their labor，用人单位应当向劳动者支付labor remuneration。labor remuneration的数额，参照本单位相同或者相近岗位劳动者的labor remuneration确定。\n" +
//            "Chapter 3　Performance and modification of labor contracts\n" +
//            "第two十Nine条　The employer and the employee shall comply with the provisions of the labor contract，全面fulfill各自的义务。\n" +
//            "第three十条　The employer shall comply with the labor contract and national regulations，向劳动者及时足额支付labor remuneration。\n" +
//            "用人单位拖欠或者未足额支付labor remuneration的，Workers can apply to the local people's court for a payment order in accordance with the law，The people's court shall issue a payment order in accordance with the law。\n" +
//            "第three十one条　Employers should strictly implement labor quota standards，Workers shall not be forced or forced in disguise to work overtime。The employer arranges overtime work，Overtime pay should be paid to workers in accordance with relevant national regulations。\n" +
//            "第three十two条　Workers refuse illegal instructions from employer managers、Forced to do risky work，Not considered a violation of the labor contract。\n" +
//            "Workers’ concerns about working conditions that endanger life safety and physical health，The right to criticize the employer、Reports and accusations。\n" +
//            "第three十three条　用人单位change名称、legal representative、Main persons in charge or investors, etc.，不影响劳动合同的fulfill。\n" +
//            "第three十Four条　The employer is merged or split, etc.，The original labor contract remains valid，劳动合同由承继其权利和义务的用人单位继续fulfill。\n" +
//            "第three十five条　用人单位与劳动者consensus，可以change劳动合同约定的content。change劳动合同，should be in writing。\n" +
//            "change后的劳动合同文本由用人单位和劳动者各执one份。\n" +
//            "Chapter 4　Cancellation and termination of labor contract\n" +
//            "第three十six条　用人单位与劳动者consensus，Labor contract can be terminated。\n" +
//            "第three十seven条　劳动者提前three十日以书面形式通知用人单位，Labor contract can be terminated。劳动者在试用期内提前three日通知用人单位，Labor contract can be terminated。\n" +
//            "第three十eight条　用人单位One of the following situations的，劳动者Labor contract can be terminated：\n" +
//            "(one)未按照劳动合同约定提供labor protection或者劳动条件的;\n" +
//            "(two)未及时足额支付labor remuneration的;\n" +
//            "(three)未依法为劳动者缴纳social insurance费的;\n" +
//            "(Four)用人单位的规章制度违反law、regulations，harming the rights and interests of workers;\n" +
//            "(five)因本法第two十six条第one款规定的情形致使劳动合同无效的;\n" +
//            "(six)law、行政法规规定劳动者Labor contract can be terminated的其他情形。\n" +
//            "The employer uses violence、Force workers to work by threatening or illegally restricting personal freedom，Or the employer gives illegal instructions、Forcing risky work to endanger the personal safety of workers，Workers can terminate the labor contract immediately，No need to notify the employer in advance。\n" +
//            "第three十Nine条　劳动者One of the following situations的，用人单位Labor contract can be terminated：\n" +
//            "(one)Those who are proven not to meet the employment conditions during the probation period;\n" +
//            "(two)Serious violation of the rules and regulations of the employer;\n" +
//            "(three)Serious dereliction of duty，fraud，Causing significant damage to the employer;\n" +
//            "(Four)劳动者同时与其他用人单位Establish labor relations，Have a serious impact on the completion of the unit's work tasks，or upon request by the employer，refuse to correct;\n" +
//            "(five)因本法第two十six条第one款第one项规定的情形致使劳动合同无效的;\n" +
//            "(six)Being held criminally responsible according to law。\n" +
//            "第Four十条　One of the following situations的，用人单位提前three十日以书面形式通知劳动者本人或者额外支付劳动者one个moon工资后，Labor contract can be terminated：\n" +
//            "(one)Workers are sick or injured not due to work，Cannot engage in the original job after expiration of the prescribed medical treatment period，Nor can they engage in work that is otherwise arranged by the employer.;\n" +
//            "(two)The worker is not qualified for the job，经过training或者调整工作岗位，Still unable to do the job;\n" +
//            "(three)劳动合同enter into时所依据的客观情况发生重大变化，致使劳动合同无法fulfill，After negotiation between the employer and the employee，未能就change劳动合同content达成协议的。\n" +
//            "第Four十one条　One of the following situations，需要裁减人员two十人以上或者裁减不足two十人但占企业职工总数百分之十以上的，用人单位提前three十日向工会或者全体职工说明情况，After listening to the opinions of the trade union or employees,，The staff reduction plan is reported to the labor administration department，Can lay off staff：\n" +
//            "(one)Reorganization in accordance with the provisions of the Enterprise Bankruptcy Law;\n" +
//            "(two)Serious difficulties occur in production and operation;\n" +
//            "(three)Enterprise conversion、Major technological innovation or adjustment of business methods，经change劳动合同后，Still need to lay off staff;\n" +
//            "(Four)其他因劳动合同enter into时所依据的客观经济情况发生重大变化，致使劳动合同无法fulfill的。\n" +
//            "When downsizing，Priority should be given to retaining the following personnel：\n" +
//            "(one)与本单位enter into较长期限的Fixed term labor contract的;\n" +
//            "(two)与本单位enter into无Fixed term labor contract的;\n" +
//            "(three)There are no other employed persons in the family，There are elderly people or minors in need of support。\n" +
//            "用人单位依照本条第one款规定裁减人员，在six个moon内重新招用人员的，Retrenched personnel should be notified，and give priority to the retrenchment of laid-off personnel under the same conditions.。\n" +
//            "第Four十two条　劳动者One of the following situations的，用人单位不得依照本法第Four十条、第Four十one条的规定解除劳动合同：\n" +
//            "(one)Workers engaged in operations exposed to occupational disease hazards do not undergo pre-job occupational health examinations，or patients with suspected occupational diseases during diagnosis or medical observation;\n" +
//            "(two)Suffering from occupational diseases or work-related injuries in the unit and confirmed to have lost or partially lost the ability to work;\n" +
//            "(three)Illness or non-work-related injury，within the prescribed medical period;\n" +
//            "(Four)Female employee during pregnancy、delivery date、lactating;\n" +
//            "(five)在本单位连续工作满十fiveYear，且距法定退休Year龄不足fiveYear的;\n" +
//            "(six)law、Other situations stipulated in administrative regulations。\n" +
//            "第Four十three条　The employer unilaterally terminates the labor contract，The reasons should be notified to the union in advance。用人单位违反law、As stipulated in administrative regulations or stipulated in the labor contract，The trade union has the right to require the employer to make corrections。Employers should study the opinions of trade unions，并将deal withresult书面通知工会。\n" +
//            "第Four十Four条　One of the following situations的，Termination of labor contract：\n" +
//            "(one)The labor contract expires;\n" +
//            "(two)Workers begin to enjoy basic pension insurance benefits in accordance with the law;\n" +
//            "(three)worker death，or declared dead or missing by the People’s Court;\n" +
//            "(Four)The employer is declared bankrupt according to law;\n" +
//            "(five)The employer's business license has been revoked、ordered to close、Cancellation or the employer decides to dissolve early;\n" +
//            "(six)law、Other situations stipulated in administrative regulations。\n" +
//            "第Four十five条　Labor contract expires，有本法第Four十two条规定情形之one的，The labor contract should be extended until the corresponding circumstances disappear.。but，本法第Four十two条第two项规定丧失或者部分丧失劳动能力劳动者的劳动合同的终止，Implemented in accordance with national regulations on work-related injury insurance。\n" +
//            "第Four十six条　One of the following situations的，Employers must pay economic compensation to workers：\n" +
//            "(one)劳动者依照本法第three十eight条规定解除劳动合同的;\n" +
//            "(two)用人单位依照本法第three十six条规定向劳动者提出解除劳动合同并与劳动者consensus解除劳动合同的;\n" +
//            "(three)用人单位依照本法第Four十条规定解除劳动合同的;\n" +
//            "(Four)用人单位依照本法第Four十one条第one款规定解除劳动合同的;\n" +
//            "(five)Unless the employer maintains or improves the conditions stipulated in the labor contract, renews the labor contract，Except when the employee does not agree to renew the contract，依照本法第Four十Four条第one项规定终止Fixed term labor contract的;\n" +
//            "(six)依照本法第Four十Four条第Four项、第five项规定终止劳动合同的;\n" +
//            "(seven)law、Other situations stipulated in administrative regulations。\n" +
//            "第Four十seven条　Economic compensation is based on the number of years the employee has worked in the unit，每满oneYear支付one个moon工资的标准向劳动者支付。six个moon以上不满oneYear的，按oneYear计算;不满six个moon的，Financial compensation of half a month’s salary to workers。\n" +
//            "The monthly salary of workers is higher than that of the municipality where the employer is located、设区的市级人民政府公布的本地区上Year度职工moon平均工资three倍的，向其支付financial compensation的标准按职工moon平均工资three倍的数额支付，向其支付financial compensation的Year限最高不超过十twoYear。\n" +
//            "本条所称moon工资是指劳动者在劳动合同解除或者终止前十two个moon的平均工资。\n" +
//            "第Four十eight条　The employer violates the provisions of this lawCancellation or termination of labor contract，劳动者要求继续fulfill劳动合同的，用人单位应当继续fulfill;劳动者不要求继续fulfill劳动合同或者劳动合同已经不能继续fulfill的，用人单位应当依照本法第eight十seven条规定支付compensation。\n" +
//            "第Four十Nine条　The state takes measures，建立健全劳动者social insurance关系跨地区转移接续制度。\n" +
//            "第five十条　用人单位应当在Cancellation or termination of labor contract时出具Cancellation or termination of labor contract的证明，并在十five日内为劳动者办理档案和social insurance关系转移手续。\n" +
//            "Workers should comply with the agreement between the parties，Handling work handover。The employer shall pay economic compensation to the employee in accordance with the relevant provisions of this Law，Pay when completing work handover。\n" +
//            "The text of the labor contract that has been terminated or terminated by the employer，至少保存twoYear备查。\n" +
//            "Chapter 5　Special provisions\n" +
//            "第one节　collective contract\n" +
//            "第five十one条　企业职工one方与用人单位通过平等协商，可以就labor remuneration、working hours、rest vacation、Labor safety and health、insurance benefits等事项enter intocollective contract。collective contract草案应当提交职工代表大会或者全体职工讨论通过。\n" +
//            "collective contract由工会代表企业职工one方与用人单位enter into;Employers without a trade union，由上级工会指导劳动者推举的代表与用人单位enter into。\n" +
//            "第five十two条　企业职工one方与用人单位可以enter intoLabor safety and health、Protection of the Rights and Interests of Female Employees、工资调整机制等专项collective contract。\n" +
//            "第five十three条　In areas below the county level，construction industry、mining industry、餐饮服务业等行业可以由工会与企业方面代表enter intoIndustrycollective contract，或者enter into区域性collective contract。\n" +
//            "第five十Four条　collective contractenter into后，Should be reported to the labor administration department;劳动行政部门自收到collective contract文本之日起十five日内未提出异议的，collective contract即行生效。\n" +
//            "依法enter into的collective contract对用人单位和劳动者具有约束力。Industry、区域性collective contract对当地本行业、Employers and workers in this area are bound。\n" +
//            "第five十five条　collective contract中labor remuneration和劳动条件等标准不得低于当地人民政府规定的最低标准;用人单位与劳动者enter into的劳动合同中labor remuneration和劳动条件等标准不得低于collective contract规定的标准。\n" +
//            "第five十six条　用人单位违反collective contract，Violating the labor rights of employees，Trade unions can require employers to take responsibility according to law;因fulfillcollective contract发生争议，Cannot be resolved through negotiation，Trade unions may apply for arbitration in accordance with the law、File a lawsuit。\n" +
//            "第two节　Labor dispatch\n" +
//            "第five十seven条　经营Labor dispatch业务应当具备下列条件：\n" +
//            "    （one）注册资本不得少于人民币two百万元；\n" +
//            "    （two）Have fixed business premises and facilities suitable for conducting business；\n" +
//            "    （three）有符合law、行政stipulated by regulationsLabor dispatch管理制度；\n" +
//            "    （Four）law、Other conditions stipulated in administrative regulations。\n" +
//            "    经营Labor dispatch业务，Application for administrative license shall be made to the labor administration department in accordance with the law；with permission，Handle corresponding company registrations in accordance with the law。without permission，任何单位和个人不得经营Labor dispatch业务。\n" +
//            "第five十eight条　Labor dispatch单位是本法所称用人单位，应当fulfill用人单位对劳动者的义务。Labor dispatch单位与被派遣劳动者enter into的劳动合同，除应当载明本法Article 17规定的事项外，The employer of the dispatched workers and the dispatch period should also be stated.、Job positions, etc.。\n" +
//            "Labor dispatch单位应当与被派遣劳动者enter intotwoYear以上的Fixed term labor contract，按moon支付labor remuneration;Dispatched workers are not working during the period，Labor dispatch单位应当按照所在地人民政府规定的最低工资标准，Pay him monthly remuneration。\n" +
//            "第five十Nine条　Labor dispatch单位派遣劳动者应当与接受以Labor dispatch形式用工的单位(Hereinafter referred to as the employing unit)enter intoLabor dispatch协议。Labor dispatch协议应当约定派遣岗位和人员数量、Dispatch period、labor remuneration和social insurance费的数额与支付方式以及违反协议的责任。\n" +
//            "用工单位应当根据工作岗位的实际需要与Labor dispatch单位确定Dispatch period，不得将连续用工期限分割enter into数个短期Labor dispatch协议。\n" +
//            "第six十条　Labor dispatch单位应当将Labor dispatch协议的content告知被派遣劳动者。\n" +
//            "Labor dispatch单位不得克扣用工单位按照Labor dispatch协议支付给被派遣劳动者的labor remuneration。\n" +
//            "Labor dispatch单位和用工单位不得向被派遣劳动者收取费用。\n" +
//            "第six十one条　Labor dispatch单位跨地区派遣劳动者的，被派遣劳动者享有的labor remuneration和劳动条件，Implemented in accordance with the standards of the location where the employer is located。\n" +
//            "第six十two条　用工单位应当fulfill下列义务：\n" +
//            "(one)Implement national labor standards，提供相应的劳动条件和labor protection;\n" +
//            "(two)告知被派遣劳动者的工作要求和labor remuneration;\n" +
//            "(three)Pay overtime、performance bonus，Provide job-related benefits;\n" +
//            "(Four)对在岗被派遣劳动者进行工作岗位所必需的training;\n" +
//            "(five)Continuously employed，Implement normal salary adjustment mechanism。\n" +
//            "Employers are not allowed to dispatch dispatched workers to other employers。\n" +
//            "第six十three条　Dispatched workers have the right to equal pay for equal work as workers in the employing unit.。Employers should follow the principle of equal pay for equal work，对被派遣劳动者与本单位同类岗位的劳动者实行相同的labor remuneration分配办法。The employer has no workers in similar positions，参照用工单位所在地相同或者相近岗位劳动者的labor remuneration确定。\n" +
//            "Labor dispatch单位与被派遣劳动者enter into的劳动合同和与用工单位enter into的Labor dispatch协议，载明或者约定的向被派遣劳动者支付的labor remuneration应当符合前款规定。\n" +
//            "第six十Four条　被派遣劳动者有权在Labor dispatch单位或者用工单位依法参加或者组织工会，Safeguard your own legitimate rights and interests。\n" +
//            "第six十five条　被派遣劳动者可以依照本法第three十six条、第three十eight条的规定与Labor dispatch单位解除劳动合同。\n" +
//            "被派遣劳动者有本法第three十Nine条和第Four十条第one项、第two项规定情形的，用工单位可以将劳动者退回Labor dispatch单位，Labor dispatch单位依照本法有关规定，The labor contract can be terminated with the employee。\n" +
//            "第six十six条　Labor contract employment is the basic employment form of enterprises in my country。Labor dispatch用工是补充形式，only on a temporary basis、Implemented in auxiliary or alternative jobs。\n" +
//            "    前款规定的临时性工作岗位是指存续时间不超过six个moon的岗位；Auxiliary jobs refer to non-main business positions that provide services for main business positions.；Alternative jobs refer to the employment unit’s workers who are studying off-duty、休假等原因无法工作的one定期间内，Positions that can be replaced by other workers。\n" +
//            "    用工单位应当严格控制Labor dispatch用工数量，不得超过其用工总量的one定比例，The specific proportion shall be stipulated by the labor administrative department of the State Council。\n" +
//            "第six十seven条　用人单位不得设立Labor dispatch单位向本单位或者所属单位派遣劳动者。\n" +
//            "第three节　Part-time employment\n" +
//            "第six十eight条　Part-time employment，It refers to hourly payment.，劳动者在同one用人单位one般平均每日working hours不超过Four小时，每周working hours累计不超过two十Four小时的用工形式。\n" +
//            "第six十Nine条　Part-time employment双方当事人可以enter into口头协议。\n" +
//            "从事Part-time employment的劳动者可以与one个或者one个以上用人单位enter into劳动合同;but，后enter into的劳动合同不得影响先enter into的劳动合同的fulfill。\n" +
//            "第seven十条　Part-time employment双方当事人No probation period is allowed。\n" +
//            "第seven十one条　Part-time employment双方当事人任何one方都可以随时通知对方Termination of employment。Termination of employment，Employers do not pay economic compensation to workers。\n" +
//            "第seven十two条　Part-time employment小时计酬标准不得低于用人单位所在地人民政府规定的最低小时工资标准。\n" +
//            "Part-time employmentlabor remuneration结算支付周期最长不得超过十five日。\n" +
//            "Chapter 6　Supervision and inspection\n" +
//            "第seven十three条　The labor administration department of the State Council is responsible for the supervision and management of the implementation of the national labor contract system.。\n" +
//            "The labor administrative departments of local people's governments at or above the county level are responsible for the supervision and management of the implementation of the labor contract system within their respective administrative regions.。\n" +
//            "The labor administrative departments of the people's governments at or above the county level shall supervise and manage the implementation of the labor contract system.，trade unions should be listened to、Opinions from enterprise representatives and relevant industry authorities。\n" +
//            "第seven十Four条　县级以上地方人民政府劳动行政部门依法对下列实施劳动合同制度的情况进行Supervision and inspection：\n" +
//            "(one)Employers formulate rules and regulations that directly involve the vital interests of workers and their implementation;\n" +
//            "(two)用人单位与劳动者enter into和解除劳动合同的情况;\n" +
//            "(three)Labor dispatch单位和用工单位遵守Labor dispatch有关规定的情况;\n" +
//            "(Four)用人单位遵守国家关于劳动者working hours和rest vacation规定的情况;\n" +
//            "(five)用人单位支付劳动合同约定的labor remuneration和执行最低工资标准的情况;\n" +
//            "(six)用人单位参加各项social insurance和缴纳social insurance费的情况;\n" +
//            "(seven)law、Other labor inspection matters prescribed by regulations。\n" +
//            "第seven十five条　县级以上地方人民政府劳动行政部门实施Supervision and inspection时，The right to inspect the labor contract、collective contract有关的材料，The right to conduct on-site inspections of workplaces，Both the employer and the employee should truthfully provide relevant information and materials。\n" +
//            "劳动行政部门的工作人员进行Supervision and inspection，Documents should be produced，Exercising powers in accordance with the law，Civilized law enforcement。\n" +
//            "第seven十six条　Construction of people's governments at or above the county level、health、The relevant competent departments such as production safety supervision and management shall within the scope of their respective responsibilities，Supervise and manage the implementation of the labor contract system by employers。\n" +
//            "第seven十seven条　The legitimate rights and interests of workers are infringed upon，Have the right to request relevant departments to handle it in accordance with the law，Or apply for arbitration according to law、File a lawsuit。\n" +
//            "第seven十eight条　工会依法Safeguard the legitimate rights and interests of workers，对用人单位fulfill劳动合同、collective contract的情况进行监督。用人单位违反劳动law、Regulations and labor contracts、collective contract的，Trade unions have the right to put forward opinions or request corrections;Workers apply for arbitration、File a lawsuit的，Trade unions provide support and assistance in accordance with the law。\n" +
//            "第seven十Nine条　Any organization or individual has the right to report violations of this law，The labor administrative department of the people's government at or above the county level shall promptly verify、deal with，and reward those who report meritoriously。\n" +
//            "Chapter 7　legal liability\n" +
//            "第eight十条　用人单位直接涉及劳动者切身利益的规章制度违反law、stipulated by regulations，Ordered by the labor administration department to make corrections，give warning;causing damage to workers，Should bear liability for compensation。\n" +
//            "第eight十one条　The labor contract text provided by the employer does not specify the necessary terms of the labor contract stipulated in this Law or the employer fails to deliver the labor contract text to the employee，Ordered by the labor administration department to make corrections;causing damage to workers，Should bear liability for compensation。\n" +
//            "第eight十two条　用人单位自用工之日起超过one个moon不满oneYear未与劳动者enter into书面劳动合同的，应当向劳动者每moon支付two倍的工资。\n" +
//            "The employer violates the provisions of this law不与劳动者enter into无Fixed term labor contract的，自应当enter into无Fixed term labor contract之日起向劳动者每moon支付two倍的工资。\n" +
//            "第eight十three条　The employer violates the provisions of this Law and agrees on a probation period with the employee，Ordered by the labor administration department to make corrections;违法约定的试用期已经fulfill的，The employer shall use the employee's full monthly salary as the standard for the probation period.，按已经fulfill的超过法定试用期的期间向劳动者支付compensation。\n" +
//            "第eight十Four条　The employer violates the provisions of this law，Seizing workers’ resident identity cards and other documents，The labor administrative department shall order the return to the worker within a time limit，并依照有关law规定给予处罚。\n" +
//            "The employer violates the provisions of this law，Collecting property from workers in the name of guarantee or other means，The labor administrative department shall order the return to the worker within a time limit，并以每人five百元以上two千元以下的标准处以罚款;causing damage to workers，Should bear liability for compensation。\n" +
//            "劳动者依法Cancellation or termination of labor contract，The employer seizes workers’ files or other items，Punished in accordance with the provisions of the preceding paragraph。\n" +
//            "第eight十five条　用人单位One of the following situations的，由劳动行政部门责令限期支付labor remuneration、Overtime pay or financial compensation;labor remuneration低于当地最低工资标准的，The difference should be paid;Overdue payment，责令用人单位按应付金额百分之five十以上百分之one百以下的标准向劳动者加付compensation：\n" +
//            "(one)未按照劳动合同的约定或者国家规定及时足额支付劳动者labor remuneration的;\n" +
//            "(two)Paying workers wages below the local minimum wage standard;\n" +
//            "(three)安排加班不Pay overtime的;\n" +
//            "(Four)Cancellation or termination of labor contract，Failure to pay economic compensation to workers in accordance with this law。\n" +
//            "第eight十six条　劳动合同依照本法第two十six条规定被确认无效，causing harm to the other party，有过错的one方Should bear liability for compensation。\n" +
//            "第eight十seven条　The employer violates the provisions of this lawCancellation or termination of labor contract的，应当依照本法第Four十seven条规定的financial compensation标准的two倍向劳动者支付compensation。\n" +
//            "第eight十eight条　用人单位One of the following situations的，Administrative penalties imposed in accordance with the law;constituting a crime，Pursuing criminal liability in accordance with the law;causing damage to workers，Should bear liability for compensation：\n" +
//            "(one)with violence、Forced labor by threatening or illegally restricting personal freedom;\n" +
//            "(two)违章指挥或者Forcing risky work to endanger the personal safety of workers;\n" +
//            "(three)insult、corporal punishment、beat、Illegal search or detention of workers;\n" +
//            "(Four)Poor working conditions、serious environmental pollution，Causing serious damage to the physical and mental health of workers。\n" +
//            "第eight十Nine条　The employer violates the provisions of this law未向劳动者出具Cancellation or termination of labor contract的书面证明，Ordered by the labor administration department to make corrections;causing damage to workers，Should bear liability for compensation。\n" +
//            "第Nine十条　The employee violates the provisions of this law and terminates the labor contract，or violate confidentiality obligations or non-competition restrictions stipulated in the employment contract，Causing losses to the employer，Should bear liability for compensation。\n" +
//            "第Nine十one条　用人单位招用与其他用人单位尚未Cancellation or termination of labor contract的劳动者，Causing losses to other employers，Shall bear joint and several liability for compensation。\n" +
//            "第Nine十two条　Violate the provisions of this law，without permission，擅自经营Labor dispatch业务的，Ordered by the labor administration department to stop illegal activities，Confiscation of illegal gains，并处违法所得one倍以上five倍以下的罚款；No illegal gains，可以处five万元以下的罚款。\n" +
//            "    Labor dispatch单位、用工单位违反本法有关Labor dispatch规定的，Ordered by the labor administration department to make corrections within a time limit；Failure to correct within the time limit，以每人five千元以上one万元以下的标准处以罚款，对Labor dispatch单位，吊销其Labor dispatch业务经营许可证。The employer causes harm to dispatched workers，Labor dispatch单位与用工单位承担连带赔偿责任。\n" +
//            "第Nine十three条　Illegal and criminal acts against employers who do not have legal business qualifications，依法追究legal liability;The laborer has already worked，该单位或者其出资人应当依照本法有关规定向劳动者支付labor remuneration、financial compensation、compensation;causing damage to workers，Should bear liability for compensation。\n" +
//            "第Nine十Four条　个人承包经营Violate the provisions of this law招用劳动者，causing damage to workers，The organization issuing the contract and the individual contractor bear joint and several liability for compensation.。\n" +
//            "第Nine十five条　Labor administrative departments and other relevant competent departments and their staff neglect their duties、不fulfill法定职责，or illegally exercise their powers，Causing damage to workers or employers，Should bear liability for compensation;Directly responsible supervisors and other directly responsible persons，Administrative sanctions shall be imposed in accordance with the law;constituting a crime，Pursuing criminal liability in accordance with the law。\n" +
//            "Chapter 8　Supplementary Provisions\n" +
//            "第Nine十six条　Public institutions与实行聘用制的工作人员enter into、fulfill、change、Cancellation or termination of labor contract，law、Administrative regulations or otherwise stipulated by the State Council，in accordance with its provisions;unspecified，Implemented in accordance with the relevant provisions of this law。\n" +
//            "第Nine十seven条　本法施行前已依法enter into且在本法施行之日存续的劳动合同，继续fulfill;本法Article 14第two款第three项规定连续enter intoFixed term labor contract的次数，自本法施行后续订Fixed term labor contract时开始计算。\n" +
//            "本法施行前已Establish labor relations，尚未enter into书面劳动合同的，应当自本法施行之日起one个moon内enter into。\n" +
//            "The labor contract existing on the date of the enforcement of this Law shall be terminated or terminated after the enforcement of this Law.，依照本法第Four十six条规定应当支付financial compensation的，financial compensationYear限自本法施行之日起计算;Before this law comes into effect, in accordance with the relevant provisions at that time，Employers must pay economic compensation to workers的，Implemented in accordance with relevant regulations at the time。\n" +
//            "第Nine十eight条　This law comes from2008Year1moon1Effective from date。\n" +
//            "\n";
//
//
//}
