package com.minio.entity;

import cn.hutool.core.io.IoUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author lyf
 * @version 1.0
 * @classname OssPolicy
 * @description 策略
 * <p>
 * <p>
 * | 参数      | 说明                                                         |
 * | --------- | ------------------------------------------------------------ |
 * | Version   | 标识策略的版本号，Minio中一般为"**2012-10-17**"              |
 * | Statement | 策略授权语句，描述策略的详细信息，包含Effect（效果）、Action（动作）、Principal（用户）、Resource（资源）和Condition（条件）。其中Condition为可选 |
 * | Effect    | Effect（效果）作用包含两种：Allow（允许）和Deny（拒绝），系统预置策略仅包含允许的授权语句，自定义策略中可以同时包含允许和拒绝的授权语句，当策略中既有允许又有拒绝的授权语句时，遵循Deny优先的原则。 |
 * | Action    | Action（动作）对资源的具体操作权限，格式为：服务名:资源类型:操作，支持单个或多个操作权限，支持通配符号*，通配符号表示所有。例如 s3:GetObject ，表示获取对象 |
 * | Resource  | Resource（资源）策略所作用的资源，支持通配符号*，通配符号表示所有。在JSON视图中，不带Resource表示对所有资源生效。Resource支持以下字符：-_0-9a-zA-Z*./\，如果Resource中包含不支持的字符，请采用通配符号*。例如：arn:aws:s3:::my-bucketname/myobject*\，表示minio中my-bucketname/myobject目录下所有对象文件。 |
 * | Condition | Condition（条件）您可以在创建自定义策略时，通过Condition元素来控制策略何时生效。Condition包括条件键和运算符，条件键表示策略语句的Condition元素，分为全局级条件键和服务级条件键。全局级条件键（前缀为g:）适用于所有操作，服务级条件键（前缀为服务缩写，如obs:）仅适用于对应服务的操作。运算符与条件键一起使用，构成完整的条件判断语句。 |
 * @since 2023/3/16 15:28
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OssPolicy {
    /**
     * 标识策略的版本号，Minio中一般为"**2012-10-17**"
     */
    @JsonProperty("Version")
    private String version = "2012-10-17";

    /**
     * 策略授权语句，描述策略的详细信息，包含
     * Effect（效果）
     * Action（动作）
     * Principal（用户）
     * Resource（资源）
     * 和Condition（条件）。
     * 其中Condition为可选
     */
    @JsonProperty("Statement")
    private Statement[] statement;

    /**
     * 获取公共读的权限json字符串
     *
     * @param bucketName 桶名称
     * @return 公共读的权限json字符串
     */
    public static String getReadOnlyJsonPolicy(String bucketName) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:GetBucketLocation\",\n" +
                "        \"s3:ListBucket\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::" + bucketName + "\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:GetObject\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    /**
     * 获取公共写的权限json字符串
     *
     * @param bucketName 桶名称
     * @return 公共写的权限json字符串
     */
    public static String getWriteOnlyJsonPolicy(String bucketName) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:GetBucketLocation\",\n" +
                "        \"s3:ListBucketMultipartUploads\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::" + bucketName + "\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:AbortMultipartUpload\",\n" +
                "        \"s3:DeleteObject\",\n" +
                "        \"s3:ListMultipartUploadParts\",\n" +
                "        \"s3:PutObject\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    /**
     * 获取公共读写的权限json字符串
     *
     * @param bucketName 桶名称
     * @return 公共读写的权限json字符串
     */
    public static String getReadWriteJsonPolicy(String bucketName) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:GetBucketLocation\",\n" +
                "        \"s3:ListBucket\",\n" +
                "        \"s3:ListBucketMultipartUploads\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::" + bucketName + "\"\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\n" +
                "        \"AWS\": [\n" +
                "          \"*\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"Action\": [\n" +
                "        \"s3:ListMultipartUploadParts\",\n" +
                "        \"s3:PutObject\",\n" +
                "        \"s3:AbortMultipartUpload\",\n" +
                "        \"s3:DeleteObject\",\n" +
                "        \"s3:GetObject\"\n" +
                "      ],\n" +
                "      \"Resource\": [\n" +
                "        \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }


    /**
     * 需要对返回值判空
     *
     * @param inputStream 输入流
     * @return 策略文件
     */
    public static String getOssPolicyByReadJsonFile(InputStream inputStream) {
        try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
            return IoUtil.readUtf8(bis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private static class Statement {
        /**
         * Effect（效果）作用包含两种：Allow（允许）和Deny（拒绝），
         * 系统预置策略仅包含允许的授权语句，
         * 自定义策略中可以同时包含允许和拒绝的授权语句，
         * 当策略中既有允许又有拒绝的授权语句时，
         * 遵循Deny优先的原则。
         */
        @JsonProperty("Effect")
        private String effect = "Allow";

        @JsonProperty("Principal")
        private Principal principal;

        /**
         * Action（动作）对资源的具体操作权限，
         * 格式为：服务名:资源类型:操作，支持单个或多个操作权限，支持通配符号*，通配符号表示所有。
         * 例如 s3:GetObject ，表示获取对象
         */
        @JsonProperty("Action")
        private String[] actions;

        /**
         * Resource（资源）策略所作用的资源，支持通配符号*，通配符号表示所有。
         * 在JSON视图中，不带Resource表示对所有资源生效。
         * Resource支持以下字符：-_0-9a-zA-Z*./\，如果Resource中包含不支持的字符，请采用通配符号*。
         * 例如：arn:aws:s3:::my-bucketname/myobject*\，表示minio中my-bucketname/myobject目录下所有对象文件。
         */
        @JsonProperty("Resource")
        private String[] resources;

        /**
         * Condition（条件）您可以在创建自定义策略时，通过Condition元素来控制策略何时生效。
         * Condition包括条件键和运算符，条件键表示策略语句的Condition元素，分为全局级条件键和服务级条件键。
         * 全局级条件键（前缀为g:）适用于所有操作，服务级条件键（前缀为服务缩写，如obs:）仅适用于对应服务的操作。
         * 运算符与条件键一起使用，构成完整的条件判断语句。
         */
        @JsonProperty("Condition")
        private String condition;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private static class Principal {
        @JsonProperty("AWS")
        private String[] aws;
    }


    public static void main(String[] args) throws JsonProcessingException {
        //System.out.println(DefaultPolicy.READ_ONLY.getPolicyJson());


        /*ObjectMapper objectMapper = new ObjectMapper();
        OssPolicy ossPolicy = new OssPolicy();
        ossPolicy.setVersion("2012-10-17");
        Statement statement = new Statement();
        statement.setEffect("Allow");
        String[] actions1 = {"admin:*"};
        statement.setActions(actions1);

        Statement statement2 = new Statement();
        statement2.setEffect("Allow");
        String[] actions2 = {"s3:*"};
        String[] resource2 = {"arn:aws:s3:::*"};
        statement2.setActions(actions2);
        statement2.setResources(resource2);

        Statement[] statements = {statement, statement2};
        ossPolicy.setStatement(statements);


        String jsonStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(ossPolicy);
        System.out.println(jsonStr);*/
    }
}
