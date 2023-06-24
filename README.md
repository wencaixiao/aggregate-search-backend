# 聚合搜索平台后端

项目介绍：基于Spring Boot + Elastic Stack + Vue3的一站式信息聚合搜索平台，用户可在同一页面集中搜索出不同来源、不同类型的内容(比如文章、图片、用户、视频等)，提升搜索体验。
主要工作：
1. 使用Knife4j + Swagger自动生成后端接口文档，便于前后端开发联调。
2. 数据源获取：使用Hutool工具包发送http请求离线获取外部网站的文章。使用jsoup实时请求bing搜索接口获取图片。
3. 为实现多类数据源的整体搜索，使用门面模式在后端对各类数据源的搜索结果进行聚合，统一返回给前端，使前端只需要发送一次请求就可以搜索出不同数据源，并通过CompletableFuture并发搜索各种数据源进一步提升搜索接口性能。
4. 为提高聚合搜索接口的通用性，通过自定义数据源接口来实现统一的数据源接入标准(比如新数据源必须支持关键词搜索、支持分页)；当新数据源(比如视频)要接入时，只需使用适配器模式对其数据查询接口进行封装、以适配数据源接口，并且使用注册器模式来代替if else来管理多个数据源对象，使调用方可根据名称轻松获取对象，无需修改原有代码，提高了系统的可扩展性。
5. 为解决文章搜不出的问题，自主搭建Elasticsearch代替MySQL的模糊查询，并通过ik分词器实现了更灵活的分词搜索。
6. 数据同步：全量同步(首次)+增量同步(新数据)。编写单次脚本实现CommandLineRunner接口，首次启动项目时会将MySQL的数据全量同步到ES中。使用Spring Scheduler定时同步近5分钟内发生更新的MySQL的文章数据到ES中。
