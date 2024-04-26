package com.synkrato.services.partner.cache;

import static com.synkrato.services.epc.common.EpcCommonConstants.COLON;
import static com.synkrato.services.epc.common.EpcCommonConstants.EMPTY_STRING;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_END;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_ERROR;
import static com.synkrato.services.epc.common.EpcCommonConstants.POUND;
import static com.synkrato.services.epc.common.EpcCommonConstants.LOGGER_START;

import static com.synkrato.services.epc.common.EpcCommonConstants.SLASH;
import static com.synkrato.services.partner.PartnerServiceConstants.S3_TOKEN_CACHE;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Slf4j
@Component
public class S3ClientCache {
  @Autowired private AmazonS3 amazonS3;

  @Cacheable(value = S3_TOKEN_CACHE, unless = "#result == null")
  public List<String> getS3ObjectKeyNames(String bucketName, String prefix) {
    log.debug("get_s3_object_key_names {}", LOGGER_START);

    List<String> keysList = null;

    try {
      ListObjectsV2Request req =
          new ListObjectsV2Request().withBucketName(bucketName).withPrefix(prefix);

      ListObjectsV2Result result;
      do {
        result = amazonS3.listObjectsV2(req);

        if (Objects.nonNull(result)) {
          keysList = Objects.isNull(keysList) ? new ArrayList<>() : keysList;
          keysList.addAll(
              result.getObjectSummaries().stream()
                  .filter(key -> !(prefix + SLASH).equals(key.getKey()))
                  .map(
                      key ->
                          key.getKey()
                              .replace(prefix + SLASH, EMPTY_STRING)
                              .replaceFirst(POUND, SLASH))
                  .collect(Collectors.toList()));

          // If there are more than maxKeys keys in the bucket, get a continuation token
          // and list the next objects.
          req.setContinuationToken(result.getNextContinuationToken());
        }

      } while (Objects.nonNull(result) && result.isTruncated());
    } catch (AmazonServiceException e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.

      log.error(
          "AmazonService Exception from S3. S3 bucket: bucket_name={}, prefix={}",
          bucketName,
          prefix,
          e);
    } catch (SdkClientException e) {
      // Amazon S3 couldn't be contacted for a response, or the client
      // couldn't parse the response from Amazon S3.
      log.error(
          "SdkClient Exception from S3. S3 bucket: bucket_name={}, prefix={}",
          bucketName,
          prefix,
          e);
    }

    log.debug("get_s3_object_key_names {}", LOGGER_END);
    return Objects.isNull(keysList) ? new ArrayList<>() : keysList;
  }

  /**
   * Get file content downloaded from S3
   *
   * @param bucketName Name of the s3 bucket
   * @param prefix prefix of the s3 bucket
   * @param fileName s3 file name
   * @return returns json content
   */
  public JSONObject getS3ObjectContent(String bucketName, String prefix, String fileName) {
    JSONObject jsonObject = null;

    try {
      S3Object s3Object =
          amazonS3.getObject(
              new GetObjectRequest(
                  bucketName + SLASH + prefix, fileName.replaceFirst(SLASH, COLON)));

      if (Objects.nonNull(s3Object)) {

        jsonObject =
            new JSONObject(
                StreamUtils.copyToString(s3Object.getObjectContent(), StandardCharsets.UTF_8));
      }
    } catch (SdkClientException | IOException | JSONException e) {
      log.error(e.getMessage(), e);
    }
    return jsonObject;
  }
}
