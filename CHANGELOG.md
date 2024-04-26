# Changelog

## [Unreleased](http://githubdev.dco.elmae/EPC/partner-service/tree/HEAD)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.5.0...HEAD)

**Security fixes:**

- IAM permission changes [\#335](http://githubdev.dco.elmae/EPC/partner-service/pull/335) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))

**Merged pull requests:**

- fix for update product throws 500 on entitlements:empty [\#336](http://githubdev.dco.elmae/EPC/partner-service/pull/336) ([jjohn](http://githubdev.dco.elmae/jjohn))

## [1.5.0](http://githubdev.dco.elmae/EPC/partner-service/tree/1.5.0) (2020-02-25)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.4.1...1.5.0)

**Closed issues:**

- AppD enhancements [\#313](http://githubdev.dco.elmae/EPC/partner-service/issues/313)

**Merged pull requests:**

- Update pom.xml [\#333](http://githubdev.dco.elmae/EPC/partner-service/pull/333) ([bappala](http://githubdev.dco.elmae/bappala))
- Update APM version, reformat start.sh [\#332](http://githubdev.dco.elmae/EPC/partner-service/pull/332) ([RArora2](http://githubdev.dco.elmae/RArora2))
- whitelisted CBC flood test product [\#331](http://githubdev.dco.elmae/EPC/partner-service/pull/331) ([tborole](http://githubdev.dco.elmae/tborole))
- Elastic APM config changes [\#330](http://githubdev.dco.elmae/EPC/partner-service/pull/330) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Throw right error message for deserialization failure [\#329](http://githubdev.dco.elmae/EPC/partner-service/pull/329) ([jjohn](http://githubdev.dco.elmae/jjohn))
- Deleting redundant OB migration scripts [\#327](http://githubdev.dco.elmae/EPC/partner-service/pull/327) ([RArora2](http://githubdev.dco.elmae/RArora2))
- webhook signingkey issue [\#326](http://githubdev.dco.elmae/EPC/partner-service/pull/326) ([jjohn](http://githubdev.dco.elmae/jjohn))
- add adhoc sql to deprecate products [\#325](http://githubdev.dco.elmae/EPC/partner-service/pull/325) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fix for invalid json content [\#324](http://githubdev.dco.elmae/EPC/partner-service/pull/324) ([jjohn](http://githubdev.dco.elmae/jjohn))
- Remove permission for NG Logger [\#323](http://githubdev.dco.elmae/EPC/partner-service/pull/323) ([RArora2](http://githubdev.dco.elmae/RArora2))
- replaced $.loan. with $. [\#322](http://githubdev.dco.elmae/EPC/partner-service/pull/322) ([sdhiver](http://githubdev.dco.elmae/sdhiver))
- Typo in start.sh [\#321](http://githubdev.dco.elmae/EPC/partner-service/pull/321) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Added elastic APM for dev [\#320](http://githubdev.dco.elmae/EPC/partner-service/pull/320) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixed OB migration sql, updated manifest to include fields [\#319](http://githubdev.dco.elmae/EPC/partner-service/pull/319) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixed OB product manifest [\#318](http://githubdev.dco.elmae/EPC/partner-service/pull/318) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Added manifest for OB test product on production [\#317](http://githubdev.dco.elmae/EPC/partner-service/pull/317) ([sdhiver](http://githubdev.dco.elmae/sdhiver))
- Exclude Deprecated Products in Get All API [\#316](http://githubdev.dco.elmae/EPC/partner-service/pull/316) ([yyan](http://githubdev.dco.elmae/yyan))
- performance improvement to avoid getSubscription call [\#298](http://githubdev.dco.elmae/EPC/partner-service/pull/298) ([jjohn](http://githubdev.dco.elmae/jjohn))

## [1.4.1](http://githubdev.dco.elmae/EPC/partner-service/tree/1.4.1) (2020-01-25)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.4.0...1.4.1)

**Closed issues:**

- Implement unit tests for access entitlements [\#263](http://githubdev.dco.elmae/EPC/partner-service/issues/263)

**Merged pull requests:**

- Fixed access entitlements for GET /products with tags [\#315](http://githubdev.dco.elmae/EPC/partner-service/pull/315) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Updated start.sh to fix AppD config [\#312](http://githubdev.dco.elmae/EPC/partner-service/pull/312) ([RArora2](http://githubdev.dco.elmae/RArora2))

## [1.4.0](http://githubdev.dco.elmae/EPC/partner-service/tree/1.4.0) (2020-01-22)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.3.0...1.4.0)

**Merged pull requests:**

- Update pom.xml for common version [\#311](http://githubdev.dco.elmae/EPC/partner-service/pull/311) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Added prod jenkins config [\#310](http://githubdev.dco.elmae/EPC/partner-service/pull/310) ([RArora2](http://githubdev.dco.elmae/RArora2))
- payload validation for invalid content [\#309](http://githubdev.dco.elmae/EPC/partner-service/pull/309) ([jjohn](http://githubdev.dco.elmae/jjohn))
- Update product | Merge patch creds delete issue [\#307](http://githubdev.dco.elmae/EPC/partner-service/pull/307) ([jjohn](http://githubdev.dco.elmae/jjohn))
- fix for requestType validation [\#306](http://githubdev.dco.elmae/EPC/partner-service/pull/306) ([jjohn](http://githubdev.dco.elmae/jjohn))
- update git terraform module to v1.1.1 [\#303](http://githubdev.dco.elmae/EPC/partner-service/pull/303) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Added sql rollback for Jan SP [\#302](http://githubdev.dco.elmae/EPC/partner-service/pull/302) ([RArora2](http://githubdev.dco.elmae/RArora2))
- fix - P2P product does not need webhooks [\#300](http://githubdev.dco.elmae/EPC/partner-service/pull/300) ([jjohn](http://githubdev.dco.elmae/jjohn))
- add model component dependency [\#297](http://githubdev.dco.elmae/EPC/partner-service/pull/297) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- unit tests for merge patch [\#296](http://githubdev.dco.elmae/EPC/partner-service/pull/296) ([jjohn](http://githubdev.dco.elmae/jjohn))
- Handle missing webhook subscription errors [\#295](http://githubdev.dco.elmae/EPC/partner-service/pull/295) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Unit tests for access entitlements and cleanup [\#294](http://githubdev.dco.elmae/EPC/partner-service/pull/294) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixes primary key constraint due to duplicate product [\#292](http://githubdev.dco.elmae/EPC/partner-service/pull/292) ([RArora2](http://githubdev.dco.elmae/RArora2))
- updated common component version [\#290](http://githubdev.dco.elmae/EPC/partner-service/pull/290) ([jjohn](http://githubdev.dco.elmae/jjohn))
- Feature/epc 13536a [\#285](http://githubdev.dco.elmae/EPC/partner-service/pull/285) ([jjohn](http://githubdev.dco.elmae/jjohn))
- validation clean up [\#270](http://githubdev.dco.elmae/EPC/partner-service/pull/270) ([jjohn](http://githubdev.dco.elmae/jjohn))
- Feature/epc 13536 b [\#267](http://githubdev.dco.elmae/EPC/partner-service/pull/267) ([jjohn](http://githubdev.dco.elmae/jjohn))
- json merge patch implementation [\#248](http://githubdev.dco.elmae/EPC/partner-service/pull/248) ([jjohn](http://githubdev.dco.elmae/jjohn))

## [1.3.0](http://githubdev.dco.elmae/EPC/partner-service/tree/1.3.0) (2019-12-20)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.2.3.RELEASE...1.3.0)

**Features:**

- Partner access entitlements [\#260](http://githubdev.dco.elmae/EPC/partner-service/pull/260) ([RArora2](http://githubdev.dco.elmae/RArora2))

**Merged pull requests:**

- Update pom.xml [\#289](http://githubdev.dco.elmae/EPC/partner-service/pull/289) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Potential CD fix [\#287](http://githubdev.dco.elmae/EPC/partner-service/pull/287) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixed NPE on access entitlement updates [\#286](http://githubdev.dco.elmae/EPC/partner-service/pull/286) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Access entitlement is now applied on all products \(prod/non-prod\) [\#284](http://githubdev.dco.elmae/EPC/partner-service/pull/284) ([RArora2](http://githubdev.dco.elmae/RArora2))
- \[WIP\] CD enhancements and cleanup on deployment jenkinsfile [\#283](http://githubdev.dco.elmae/EPC/partner-service/pull/283) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Update Jenkinsfile for correct branch [\#282](http://githubdev.dco.elmae/EPC/partner-service/pull/282) ([RArora2](http://githubdev.dco.elmae/RArora2))
- CD is now integrated with CI [\#281](http://githubdev.dco.elmae/EPC/partner-service/pull/281) ([RArora2](http://githubdev.dco.elmae/RArora2))
- throw 400 read only for partner passing integrationType other than ASYNC [\#280](http://githubdev.dco.elmae/EPC/partner-service/pull/280) ([yyan](http://githubdev.dco.elmae/yyan))
- Throw 403 if found dismatch partnerId [\#279](http://githubdev.dco.elmae/EPC/partner-service/pull/279) ([yyan](http://githubdev.dco.elmae/yyan))
- Fixes environment variable format [\#277](http://githubdev.dco.elmae/EPC/partner-service/pull/277) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixes application jwt issue [\#275](http://githubdev.dco.elmae/EPC/partner-service/pull/275) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Enables on build number for health endpoint [\#274](http://githubdev.dco.elmae/EPC/partner-service/pull/274) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixes docker issue [\#273](http://githubdev.dco.elmae/EPC/partner-service/pull/273) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixes partner service s3 bucket for cert [\#272](http://githubdev.dco.elmae/EPC/partner-service/pull/272) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Java is now executed with exec [\#271](http://githubdev.dco.elmae/EPC/partner-service/pull/271) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Changed epc-common.version [\#269](http://githubdev.dco.elmae/EPC/partner-service/pull/269) ([yyan](http://githubdev.dco.elmae/yyan))
- Jenkins CI pipeline changes [\#268](http://githubdev.dco.elmae/EPC/partner-service/pull/268) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Fixes SQL script for allow/deny [\#266](http://githubdev.dco.elmae/EPC/partner-service/pull/266) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Adds null check for allow/deny before validation [\#265](http://githubdev.dco.elmae/EPC/partner-service/pull/265) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Integration Type is now implicitly derived [\#261](http://githubdev.dco.elmae/EPC/partner-service/pull/261) ([yyan](http://githubdev.dco.elmae/yyan))

## [1.2.3.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.2.3.RELEASE) (2019-11-26)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.2.3.1...1.2.3.RELEASE)

**Merged pull requests:**

- update epc common component to 1.5.0.RELEASE [\#262](http://githubdev.dco.elmae/EPC/partner-service/pull/262) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- add null check for credential properties when building credential entity [\#259](http://githubdev.dco.elmae/EPC/partner-service/pull/259) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Partner ID should be Implicit for Partner Originated API Calls [\#258](http://githubdev.dco.elmae/EPC/partner-service/pull/258) ([yyan](http://githubdev.dco.elmae/yyan))
- adds whitelisting to both env zones [\#257](http://githubdev.dco.elmae/EPC/partner-service/pull/257) ([RArora2](http://githubdev.dco.elmae/RArora2))
- QA offshore whitelist IPs [\#256](http://githubdev.dco.elmae/EPC/partner-service/pull/256) ([RArora2](http://githubdev.dco.elmae/RArora2))
- standardize error message when invoking validator explicitly [\#254](http://githubdev.dco.elmae/EPC/partner-service/pull/254) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Remove elli\_ity for internal admin [\#253](http://githubdev.dco.elmae/EPC/partner-service/pull/253) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- update propertytype values for credential object [\#252](http://githubdev.dco.elmae/EPC/partner-service/pull/252) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Standardize error messages [\#246](http://githubdev.dco.elmae/EPC/partner-service/pull/246) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))

## [1.2.3.1](http://githubdev.dco.elmae/EPC/partner-service/tree/1.2.3.1) (2019-11-10)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.2.2.RELEASE...1.2.3.1)

## [1.2.2.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.2.2.RELEASE) (2019-10-31)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.2.1...1.2.2.RELEASE)

## [1.2.1](http://githubdev.dco.elmae/EPC/partner-service/tree/1.2.1) (2019-10-31)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.2.0...1.2.1)

**Merged pull requests:**

- fix release failure for 1.2.1-SNAPSHOT [\#250](http://githubdev.dco.elmae/EPC/partner-service/pull/250) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))

## [1.2.0](http://githubdev.dco.elmae/EPC/partner-service/tree/1.2.0) (2019-10-31)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.1.0.RELEASE...1.2.0)

**Merged pull requests:**

- Bug/epc 13558 [\#249](http://githubdev.dco.elmae/EPC/partner-service/pull/249) ([jjohn](http://githubdev.dco.elmae/jjohn))
- do not enforce environment when checking for dup products by partner … [\#247](http://githubdev.dco.elmae/EPC/partner-service/pull/247) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- prepare for next development iteration [\#242](http://githubdev.dco.elmae/EPC/partner-service/pull/242) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))

## [1.1.0.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.1.0.RELEASE) (2019-10-24)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.12.RELEASE...1.1.0.RELEASE)

**Fixed bugs:**

- Fix for Null Entitlements. It is now Defaulted to empty lists for Access entitlements [\#210](http://githubdev.dco.elmae/EPC/partner-service/pull/210) ([RArora2](http://githubdev.dco.elmae/RArora2))

**Merged pull requests:**

- Release/1.1.0.release [\#243](http://githubdev.dco.elmae/EPC/partner-service/pull/243) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- update common component version to 1.4.0.RELEASE [\#241](http://githubdev.dco.elmae/EPC/partner-service/pull/241) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- bug/jfrog-issue [\#239](http://githubdev.dco.elmae/EPC/partner-service/pull/239) ([styagi](http://githubdev.dco.elmae/styagi))
- update error code for product request type removal [\#238](http://githubdev.dco.elmae/EPC/partner-service/pull/238) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- added jmeter script of partner service APIs [\#237](http://githubdev.dco.elmae/EPC/partner-service/pull/237) ([jjohn](http://githubdev.dco.elmae/jjohn))
- EPC-13023 fixes duplicate error for invalid webhooks. [\#236](http://githubdev.dco.elmae/EPC/partner-service/pull/236) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- Removed SignalFx metrics [\#235](http://githubdev.dco.elmae/EPC/partner-service/pull/235) ([styagi](http://githubdev.dco.elmae/styagi))
- make credential view as default for find all [\#234](http://githubdev.dco.elmae/EPC/partner-service/pull/234) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- environment validation changes [\#233](http://githubdev.dco.elmae/EPC/partner-service/pull/233) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Bug/epc 13295 [\#232](http://githubdev.dco.elmae/EPC/partner-service/pull/232) ([jjohn](http://githubdev.dco.elmae/jjohn))
- credential view changes [\#231](http://githubdev.dco.elmae/EPC/partner-service/pull/231) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- remove additional sorting [\#229](http://githubdev.dco.elmae/EPC/partner-service/pull/229) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- do not have environment when checking for dup product [\#228](http://githubdev.dco.elmae/EPC/partner-service/pull/228) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- EPC-13249 fixes swagger for partner service [\#227](http://githubdev.dco.elmae/EPC/partner-service/pull/227) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- hide productSearchMap attribute from Swagger for get all products [\#226](http://githubdev.dco.elmae/EPC/partner-service/pull/226) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Create pull\_request\_template.md [\#225](http://githubdev.dco.elmae/EPC/partner-service/pull/225) ([RArora2](http://githubdev.dco.elmae/RArora2))
- elli ity changes for lender environment [\#224](http://githubdev.dco.elmae/EPC/partner-service/pull/224) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fix create product url regression on header [\#223](http://githubdev.dco.elmae/EPC/partner-service/pull/223) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fix missing semi colon in update statement [\#222](http://githubdev.dco.elmae/EPC/partner-service/pull/222) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fix npe due to empty json path [\#221](http://githubdev.dco.elmae/EPC/partner-service/pull/221) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- add credential attribute to integrationType view [\#220](http://githubdev.dco.elmae/EPC/partner-service/pull/220) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- null check when converting environment to lowercase [\#219](http://githubdev.dco.elmae/EPC/partner-service/pull/219) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fix count query for native query using tags [\#218](http://githubdev.dco.elmae/EPC/partner-service/pull/218) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fix regression due to incorrect validator usage [\#217](http://githubdev.dco.elmae/EPC/partner-service/pull/217) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- get all products with tags [\#216](http://githubdev.dco.elmae/EPC/partner-service/pull/216) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- enforce tag keys [\#215](http://githubdev.dco.elmae/EPC/partner-service/pull/215) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fix updated/by refresh issue [\#214](http://githubdev.dco.elmae/EPC/partner-service/pull/214) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Check for non admin when updating readonly attributes [\#213](http://githubdev.dco.elmae/EPC/partner-service/pull/213) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fixes error type for illegalarguement during marshalling [\#212](http://githubdev.dco.elmae/EPC/partner-service/pull/212) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Excluding slave 6 for deployment \(Jfrog issue on 6\) [\#211](http://githubdev.dco.elmae/EPC/partner-service/pull/211) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixes vars and methods in line with latest common [\#209](http://githubdev.dco.elmae/EPC/partner-service/pull/209) ([RArora2](http://githubdev.dco.elmae/RArora2))
- implement epc internal admin token [\#208](http://githubdev.dco.elmae/EPC/partner-service/pull/208) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Removes slave07 [\#207](http://githubdev.dco.elmae/EPC/partner-service/pull/207) ([RArora2](http://githubdev.dco.elmae/RArora2))
- add null check for product requesttype validation [\#206](http://githubdev.dco.elmae/EPC/partner-service/pull/206) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Remove slave09 [\#205](http://githubdev.dco.elmae/EPC/partner-service/pull/205) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Migrate existing data entitlements to new schema to support multiple request types [\#204](http://githubdev.dco.elmae/EPC/partner-service/pull/204) ([bappala](http://githubdev.dco.elmae/bappala))
- Feature/epc 12484 [\#203](http://githubdev.dco.elmae/EPC/partner-service/pull/203) ([styagi](http://githubdev.dco.elmae/styagi))
- removeResponseColumn from comments [\#202](http://githubdev.dco.elmae/EPC/partner-service/pull/202) ([bappala](http://githubdev.dco.elmae/bappala))
- JsonPath validation for data entitlements [\#201](http://githubdev.dco.elmae/EPC/partner-service/pull/201) ([bappala](http://githubdev.dco.elmae/bappala))
- PLM Extensions Representation [\#198](http://githubdev.dco.elmae/EPC/partner-service/pull/198) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Bug/rebase to development [\#197](http://githubdev.dco.elmae/EPC/partner-service/pull/197) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Access Entitlements representation [\#192](http://githubdev.dco.elmae/EPC/partner-service/pull/192) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Data entitlement request type changes [\#191](http://githubdev.dco.elmae/EPC/partner-service/pull/191) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fixes versions [\#189](http://githubdev.dco.elmae/EPC/partner-service/pull/189) ([RArora2](http://githubdev.dco.elmae/RArora2))
- adminInterfaceUrl to november\_sp [\#188](http://githubdev.dco.elmae/EPC/partner-service/pull/188) ([styagi](http://githubdev.dco.elmae/styagi))

## [1.0.12.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.12.RELEASE) (2019-09-10)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.11.1.RELEASE...1.0.12.RELEASE)

**Merged pull requests:**

- update epc common component version [\#196](http://githubdev.dco.elmae/EPC/partner-service/pull/196) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Docker fix [\#195](http://githubdev.dco.elmae/EPC/partner-service/pull/195) ([tborole](http://githubdev.dco.elmae/tborole))
- defect/epc 12925 - Update product attributes [\#194](http://githubdev.dco.elmae/EPC/partner-service/pull/194) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- update common component version [\#193](http://githubdev.dco.elmae/EPC/partner-service/pull/193) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- adding aapd agents only for int and prod [\#190](http://githubdev.dco.elmae/EPC/partner-service/pull/190) ([tborole](http://githubdev.dco.elmae/tborole))
- update epc common component for xss changes [\#186](http://githubdev.dco.elmae/EPC/partner-service/pull/186) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- EPC-12761 : appd integration [\#184](http://githubdev.dco.elmae/EPC/partner-service/pull/184) ([tborole](http://githubdev.dco.elmae/tborole))
- EPC-12847 for PSS errors [\#183](http://githubdev.dco.elmae/EPC/partner-service/pull/183) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- removed type from credential object [\#176](http://githubdev.dco.elmae/EPC/partner-service/pull/176) ([bappala](http://githubdev.dco.elmae/bappala))
- EPC-12767:removed unused import [\#175](http://githubdev.dco.elmae/EPC/partner-service/pull/175) ([bappala](http://githubdev.dco.elmae/bappala))
- EPC-12767 [\#174](http://githubdev.dco.elmae/EPC/partner-service/pull/174) ([bappala](http://githubdev.dco.elmae/bappala))
- Feature/epc 11895 [\#173](http://githubdev.dco.elmae/EPC/partner-service/pull/173) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- Feature/epc 11895 [\#168](http://githubdev.dco.elmae/EPC/partner-service/pull/168) ([ksidhu](http://githubdev.dco.elmae/ksidhu))

## [1.0.11.1.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.11.1.RELEASE) (2019-08-28)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.11.RELEASE...1.0.11.1.RELEASE)

**Merged pull requests:**

- change all release to 11.1 snap [\#178](http://githubdev.dco.elmae/EPC/partner-service/pull/178) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Hotfix/epc 12767 [\#177](http://githubdev.dco.elmae/EPC/partner-service/pull/177) ([bappala](http://githubdev.dco.elmae/bappala))

## [1.0.11.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.11.RELEASE) (2019-08-27)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.7.1.RELEASE...1.0.11.RELEASE)

**Merged pull requests:**

- update epc common component [\#170](http://githubdev.dco.elmae/EPC/partner-service/pull/170) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Merging 19.3 September SP development branch [\#169](http://githubdev.dco.elmae/EPC/partner-service/pull/169) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Revert "Epc 12484" [\#167](http://githubdev.dco.elmae/EPC/partner-service/pull/167) ([styagi](http://githubdev.dco.elmae/styagi))
- Revert " Added feature to search with params feature/EPC-12413" [\#166](http://githubdev.dco.elmae/EPC/partner-service/pull/166) ([RArora2](http://githubdev.dco.elmae/RArora2))
- read me has transaction service endpoint for prod. Corrected to partn… [\#165](http://githubdev.dco.elmae/EPC/partner-service/pull/165) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 12588 [\#164](http://githubdev.dco.elmae/EPC/partner-service/pull/164) ([bappala](http://githubdev.dco.elmae/bappala))
- Sqlscript fix [\#163](http://githubdev.dco.elmae/EPC/partner-service/pull/163) ([jjohn](http://githubdev.dco.elmae/jjohn))
- Epc 12484 [\#161](http://githubdev.dco.elmae/EPC/partner-service/pull/161) ([styagi](http://githubdev.dco.elmae/styagi))
- update product annotation based validation happens for credential schema [\#155](http://githubdev.dco.elmae/EPC/partner-service/pull/155) ([jjohn](http://githubdev.dco.elmae/jjohn))

## [1.0.7.1.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.7.1.RELEASE) (2019-08-09)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.10.RELEASE...1.0.7.1.RELEASE)

## [1.0.10.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.10.RELEASE) (2019-08-06)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.9.RELEASE...1.0.10.RELEASE)

**Merged pull requests:**

- Bug/log fix [\#160](http://githubdev.dco.elmae/EPC/partner-service/pull/160) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Release/1 0 7 [\#149](http://githubdev.dco.elmae/EPC/partner-service/pull/149) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Fixes slave issue [\#125](http://githubdev.dco.elmae/EPC/partner-service/pull/125) ([RArora2](http://githubdev.dco.elmae/RArora2))
- 1.0.6.RELEASE [\#121](http://githubdev.dco.elmae/EPC/partner-service/pull/121) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- 1 0 5 release [\#119](http://githubdev.dco.elmae/EPC/partner-service/pull/119) ([RArora2](http://githubdev.dco.elmae/RArora2))
- 1 0 4 release [\#115](http://githubdev.dco.elmae/EPC/partner-service/pull/115) ([RArora2](http://githubdev.dco.elmae/RArora2))

## [1.0.9.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.9.RELEASE) (2019-08-05)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.8.RELEASE...1.0.9.RELEASE)

**Merged pull requests:**

- EC-12411 : TOP integration [\#158](http://githubdev.dco.elmae/EPC/partner-service/pull/158) ([tborole](http://githubdev.dco.elmae/tborole))
- Enum Fix [\#156](http://githubdev.dco.elmae/EPC/partner-service/pull/156) ([styagi](http://githubdev.dco.elmae/styagi))

## [1.0.8.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.8.RELEASE) (2019-07-31)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.7.RELEASE...1.0.8.RELEASE)

**Merged pull requests:**

- Partner validator unit TC [\#153](http://githubdev.dco.elmae/EPC/partner-service/pull/153) ([styagi](http://githubdev.dco.elmae/styagi))
- feature/epc-11809:Apprasial RI Test Cases [\#152](http://githubdev.dco.elmae/EPC/partner-service/pull/152) ([bappala](http://githubdev.dco.elmae/bappala))
-  Added feature to search with params feature/EPC-12413 [\#150](http://githubdev.dco.elmae/EPC/partner-service/pull/150) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- implemented credential schema [\#147](http://githubdev.dco.elmae/EPC/partner-service/pull/147) ([jjohn](http://githubdev.dco.elmae/jjohn))
- feature/epc-11808:Adding manifest test cases [\#145](http://githubdev.dco.elmae/EPC/partner-service/pull/145) ([bappala](http://githubdev.dco.elmae/bappala))

## [1.0.7.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.7.RELEASE) (2019-07-19)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.6.RELEASE...1.0.7.RELEASE)

**Closed issues:**

- test [\#140](http://githubdev.dco.elmae/EPC/partner-service/issues/140)

**Merged pull requests:**

- using common component release version [\#148](http://githubdev.dco.elmae/EPC/partner-service/pull/148) ([tborole](http://githubdev.dco.elmae/tborole))
-  Removed default view [\#146](http://githubdev.dco.elmae/EPC/partner-service/pull/146) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- Request Type empty and null check  [\#144](http://githubdev.dco.elmae/EPC/partner-service/pull/144) ([styagi](http://githubdev.dco.elmae/styagi))
- EPC-12282  - added produce APPLICATION\_JSON\_VALUE [\#143](http://githubdev.dco.elmae/EPC/partner-service/pull/143) ([styagi](http://githubdev.dco.elmae/styagi))
- Added null check on tags [\#142](http://githubdev.dco.elmae/EPC/partner-service/pull/142) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- Log fix for local environment [\#141](http://githubdev.dco.elmae/EPC/partner-service/pull/141) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Bug fix for search tags [\#139](http://githubdev.dco.elmae/EPC/partner-service/pull/139) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- EPC-12243:change the query parameter from name to productName [\#138](http://githubdev.dco.elmae/EPC/partner-service/pull/138) ([bappala](http://githubdev.dco.elmae/bappala))
- Request Type check EPC-12234 [\#137](http://githubdev.dco.elmae/EPC/partner-service/pull/137) ([styagi](http://githubdev.dco.elmae/styagi))
- remove duplcate entry of bottomRatioPercent [\#136](http://githubdev.dco.elmae/EPC/partner-service/pull/136) ([bappala](http://githubdev.dco.elmae/bappala))
- Enabled pre-commit hooks for codestyling [\#135](http://githubdev.dco.elmae/EPC/partner-service/pull/135) ([RArora2](http://githubdev.dco.elmae/RArora2))
- remove deplicate base amount from OB manifest [\#134](http://githubdev.dco.elmae/EPC/partner-service/pull/134) ([bappala](http://githubdev.dco.elmae/bappala))
- Unauthorization fix on invalid JWTs [\#133](http://githubdev.dco.elmae/EPC/partner-service/pull/133) ([RArora2](http://githubdev.dco.elmae/RArora2))
- messageUtil.getMessage fixed [\#132](http://githubdev.dco.elmae/EPC/partner-service/pull/132) ([styagi](http://githubdev.dco.elmae/styagi))
- fix for resource validation [\#131](http://githubdev.dco.elmae/EPC/partner-service/pull/131) ([jjohn](http://githubdev.dco.elmae/jjohn))
- changed webhook resource name to urn format [\#128](http://githubdev.dco.elmae/EPC/partner-service/pull/128) ([jjohn](http://githubdev.dco.elmae/jjohn))
- fix for webhook resource validation [\#124](http://githubdev.dco.elmae/EPC/partner-service/pull/124) ([jjohn](http://githubdev.dco.elmae/jjohn))
- dup check for webhook resource name [\#123](http://githubdev.dco.elmae/EPC/partner-service/pull/123) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- remove Tax identifier [\#122](http://githubdev.dco.elmae/EPC/partner-service/pull/122) ([bappala](http://githubdev.dco.elmae/bappala))
- Search tags with review comments [\#117](http://githubdev.dco.elmae/EPC/partner-service/pull/117) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- Product Update API and Validation  [\#101](http://githubdev.dco.elmae/EPC/partner-service/pull/101) ([styagi](http://githubdev.dco.elmae/styagi))
- Postman is now moved to common [\#99](http://githubdev.dco.elmae/EPC/partner-service/pull/99) ([RArora2](http://githubdev.dco.elmae/RArora2))

## [1.0.6.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.6.RELEASE) (2019-06-26)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.5.RELEASE...1.0.6.RELEASE)

**Merged pull requests:**

- webhookendpoint fix [\#120](http://githubdev.dco.elmae/EPC/partner-service/pull/120) ([bappala](http://githubdev.dco.elmae/bappala))

## [1.0.5.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.5.RELEASE) (2019-06-25)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.4.RELEASE...1.0.5.RELEASE)

**Merged pull requests:**

- Bump up common version [\#118](http://githubdev.dco.elmae/EPC/partner-service/pull/118) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Defect fix EPC-12021 [\#116](http://githubdev.dco.elmae/EPC/partner-service/pull/116) ([styagi](http://githubdev.dco.elmae/styagi))
- View changes [\#114](http://githubdev.dco.elmae/EPC/partner-service/pull/114) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))

## [1.0.4.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.4.RELEASE) (2019-06-20)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.3.RELEASE...1.0.4.RELEASE)

**Merged pull requests:**

- Fix for conflicts between master and development [\#113](http://githubdev.dco.elmae/EPC/partner-service/pull/113) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Update terraform version to 11 [\#89](http://githubdev.dco.elmae/EPC/partner-service/pull/89) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Adds envfix [\#78](http://githubdev.dco.elmae/EPC/partner-service/pull/78) ([RArora2](http://githubdev.dco.elmae/RArora2))
- EPC-11444 : removed overriding on env zone [\#76](http://githubdev.dco.elmae/EPC/partner-service/pull/76) ([tborole](http://githubdev.dco.elmae/tborole))

## [1.0.3.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.3.RELEASE) (2019-06-20)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.1.RELEASE...1.0.3.RELEASE)

**Fixed bugs:**

- Adds missing ecs\_cluster\_name var [\#108](http://githubdev.dco.elmae/EPC/partner-service/pull/108) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Passive count fix [\#64](http://githubdev.dco.elmae/EPC/partner-service/pull/64) ([RArora2](http://githubdev.dco.elmae/RArora2))

**Merged pull requests:**

- Bump up common component version [\#112](http://githubdev.dco.elmae/EPC/partner-service/pull/112) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Epc 11762 [\#111](http://githubdev.dco.elmae/EPC/partner-service/pull/111) ([RArora2](http://githubdev.dco.elmae/RArora2))
- update common component version [\#110](http://githubdev.dco.elmae/EPC/partner-service/pull/110) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Removes wrong setup for sonar [\#109](http://githubdev.dco.elmae/EPC/partner-service/pull/109) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Epc 11762 [\#107](http://githubdev.dco.elmae/EPC/partner-service/pull/107) ([bappala](http://githubdev.dco.elmae/bappala))
- auth check for get products [\#106](http://githubdev.dco.elmae/EPC/partner-service/pull/106) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fixup! Webhook resource validation \(EPC-EPC-11900\) [\#104](http://githubdev.dco.elmae/EPC/partner-service/pull/104) ([styagi](http://githubdev.dco.elmae/styagi))
- saveandflush product creation to catch any db errors ahead for subscr… [\#103](http://githubdev.dco.elmae/EPC/partner-service/pull/103) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- EPC-11899:No data node in create product throws 500 Internal server e… [\#102](http://githubdev.dco.elmae/EPC/partner-service/pull/102) ([bappala](http://githubdev.dco.elmae/bappala))
- revert d2b02116458db14a60586587e8e21cb2cc9bc53e [\#100](http://githubdev.dco.elmae/EPC/partner-service/pull/100) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- EPC-11870 Onboard Partner productsAPI on API Gateway for Partners [\#98](http://githubdev.dco.elmae/EPC/partner-service/pull/98) ([ksidhu](http://githubdev.dco.elmae/ksidhu))
- manifest changes for residences [\#97](http://githubdev.dco.elmae/EPC/partner-service/pull/97) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- add webhook token cache and concurrent subscription creation [\#96](http://githubdev.dco.elmae/EPC/partner-service/pull/96) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Product integ [\#95](http://githubdev.dco.elmae/EPC/partner-service/pull/95) ([bappala](http://githubdev.dco.elmae/bappala))
- base product dto changes [\#94](http://githubdev.dco.elmae/EPC/partner-service/pull/94) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Updated with latest development  [\#93](http://githubdev.dco.elmae/EPC/partner-service/pull/93) ([styagi](http://githubdev.dco.elmae/styagi))
- Create product table pull request [\#91](http://githubdev.dco.elmae/EPC/partner-service/pull/91) ([styagi](http://githubdev.dco.elmae/styagi))
- epc-11693:adding location in response header [\#90](http://githubdev.dco.elmae/EPC/partner-service/pull/90) ([bappala](http://githubdev.dco.elmae/bappala))
- fix tf version to 11 instead of latest [\#88](http://githubdev.dco.elmae/EPC/partner-service/pull/88) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- fix primary key and unique index [\#87](http://githubdev.dco.elmae/EPC/partner-service/pull/87) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Replace Uuid with String [\#86](http://githubdev.dco.elmae/EPC/partner-service/pull/86) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- EPC-11444 : modification for env fix and jenkins slaves [\#84](http://githubdev.dco.elmae/EPC/partner-service/pull/84) ([tborole](http://githubdev.dco.elmae/tborole))
- remove version changes:EPC-11372 [\#83](http://githubdev.dco.elmae/EPC/partner-service/pull/83) ([bappala](http://githubdev.dco.elmae/bappala))
- update vod finicity manifest to be in sync with finicity api attributes [\#81](http://githubdev.dco.elmae/EPC/partner-service/pull/81) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Voa manifest and created/updated column changes [\#80](http://githubdev.dco.elmae/EPC/partner-service/pull/80) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- removeAttributes from fields object [\#73](http://githubdev.dco.elmae/EPC/partner-service/pull/73) ([bappala](http://githubdev.dco.elmae/bappala))
- updated concept db url [\#72](http://githubdev.dco.elmae/EPC/partner-service/pull/72) ([tborole](http://githubdev.dco.elmae/tborole))
- Update jenkinsfile for slave excluding slaves [\#71](http://githubdev.dco.elmae/EPC/partner-service/pull/71) ([RArora2](http://githubdev.dco.elmae/RArora2))
- EPC-11444 : added concept db url [\#70](http://githubdev.dco.elmae/EPC/partner-service/pull/70) ([tborole](http://githubdev.dco.elmae/tborole))
- Update application.yml [\#69](http://githubdev.dco.elmae/EPC/partner-service/pull/69) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Finicity VOD Manifest [\#68](http://githubdev.dco.elmae/EPC/partner-service/pull/68) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Use latest common snapshot [\#67](http://githubdev.dco.elmae/EPC/partner-service/pull/67) ([tborole](http://githubdev.dco.elmae/tborole))
- 19.3 [\#66](http://githubdev.dco.elmae/EPC/partner-service/pull/66) ([RArora2](http://githubdev.dco.elmae/RArora2))
- changed class name from manifestUtil to PartnerManifestUtil [\#65](http://githubdev.dco.elmae/EPC/partner-service/pull/65) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 11187 [\#63](http://githubdev.dco.elmae/EPC/partner-service/pull/63) ([bappala](http://githubdev.dco.elmae/bappala))
- Adds stage db url cname [\#62](http://githubdev.dco.elmae/EPC/partner-service/pull/62) ([RArora2](http://githubdev.dco.elmae/RArora2))
- manifest json fix [\#61](http://githubdev.dco.elmae/EPC/partner-service/pull/61) ([bappala](http://githubdev.dco.elmae/bappala))
- Adds prod db url [\#60](http://githubdev.dco.elmae/EPC/partner-service/pull/60) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Deployment files code refactor [\#59](http://githubdev.dco.elmae/EPC/partner-service/pull/59) ([RArora2](http://githubdev.dco.elmae/RArora2))
- PartnerManifestFix [\#58](http://githubdev.dco.elmae/EPC/partner-service/pull/58) ([bappala](http://githubdev.dco.elmae/bappala))
- fixing test cases [\#57](http://githubdev.dco.elmae/EPC/partner-service/pull/57) ([bappala](http://githubdev.dco.elmae/bappala))
- Code style fix 2 [\#55](http://githubdev.dco.elmae/EPC/partner-service/pull/55) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Codestyle test [\#54](http://githubdev.dco.elmae/EPC/partner-service/pull/54) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Epc 9449 applymanifest [\#53](http://githubdev.dco.elmae/EPC/partner-service/pull/53) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9449 : merging all apply manifest changes [\#51](http://githubdev.dco.elmae/EPC/partner-service/pull/51) ([bappala](http://githubdev.dco.elmae/bappala))

## [1.0.1.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.1.RELEASE) (2019-04-11)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/1.0.0.RELEASE...1.0.1.RELEASE)

**Merged pull requests:**

- Adds stage db for partner [\#50](http://githubdev.dco.elmae/EPC/partner-service/pull/50) ([RArora2](http://githubdev.dco.elmae/RArora2))

## [1.0.0.RELEASE](http://githubdev.dco.elmae/EPC/partner-service/tree/1.0.0.RELEASE) (2019-04-04)

[Full Changelog](http://githubdev.dco.elmae/EPC/partner-service/compare/3465c63f82d35d764b0de3372787f9bceed1c3ba...1.0.0.RELEASE)

**Fixed bugs:**

- Fixes dimensions for scale up alarm [\#37](http://githubdev.dco.elmae/EPC/partner-service/pull/37) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Modifies dimensions input for cloudmetrics [\#34](http://githubdev.dco.elmae/EPC/partner-service/pull/34) ([RArora2](http://githubdev.dco.elmae/RArora2))

**Merged pull requests:**

- Pom fixes, ecs fixes [\#49](http://githubdev.dco.elmae/EPC/partner-service/pull/49) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Version update for epc common [\#48](http://githubdev.dco.elmae/EPC/partner-service/pull/48) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Code style [\#47](http://githubdev.dco.elmae/EPC/partner-service/pull/47) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Adds checkstyle and fixes issues [\#44](http://githubdev.dco.elmae/EPC/partner-service/pull/44) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Yml fix [\#43](http://githubdev.dco.elmae/EPC/partner-service/pull/43) ([RArora2](http://githubdev.dco.elmae/RArora2))
- add postman collection [\#42](http://githubdev.dco.elmae/EPC/partner-service/pull/42) ([bappala](http://githubdev.dco.elmae/bappala))
- Added back iprange tf scripts [\#41](http://githubdev.dco.elmae/EPC/partner-service/pull/41) ([RArora2](http://githubdev.dco.elmae/RArora2))
- trying to increase the test coverage [\#40](http://githubdev.dco.elmae/EPC/partner-service/pull/40) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 10681 [\#39](http://githubdev.dco.elmae/EPC/partner-service/pull/39) ([bappala](http://githubdev.dco.elmae/bappala))
- Deployment fixes [\#38](http://githubdev.dco.elmae/EPC/partner-service/pull/38) ([RArora2](http://githubdev.dco.elmae/RArora2))
- returning entity object as default view [\#36](http://githubdev.dco.elmae/EPC/partner-service/pull/36) ([bappala](http://githubdev.dco.elmae/bappala))
- changed to PEG connectionString [\#35](http://githubdev.dco.elmae/EPC/partner-service/pull/35) ([bappala](http://githubdev.dco.elmae/bappala))
- Whitelist epc2.0 ips [\#33](http://githubdev.dco.elmae/EPC/partner-service/pull/33) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Clean up and partner url changes [\#32](http://githubdev.dco.elmae/EPC/partner-service/pull/32) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Update autoservice ip var name [\#30](http://githubdev.dco.elmae/EPC/partner-service/pull/30) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- add common properties to classpath [\#29](http://githubdev.dco.elmae/EPC/partner-service/pull/29) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- change log level to INFO [\#28](http://githubdev.dco.elmae/EPC/partner-service/pull/28) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- partner service code review changes [\#27](http://githubdev.dco.elmae/EPC/partner-service/pull/27) ([tponnusamy](http://githubdev.dco.elmae/tponnusamy))
- Removed distribution repo and added snapshot repo for artifactory [\#25](http://githubdev.dco.elmae/EPC/partner-service/pull/25) ([RArora2](http://githubdev.dco.elmae/RArora2))
- Manifest logic [\#24](http://githubdev.dco.elmae/EPC/partner-service/pull/24) ([bappala](http://githubdev.dco.elmae/bappala))
- Removed logging of claims, reported by Checkmarx [\#23](http://githubdev.dco.elmae/EPC/partner-service/pull/23) ([RArora2](http://githubdev.dco.elmae/RArora2))
- changed SSL validity to 1year [\#21](http://githubdev.dco.elmae/EPC/partner-service/pull/21) ([bappala](http://githubdev.dco.elmae/bappala))
- added error handling and changed ID from string to UUID [\#20](http://githubdev.dco.elmae/EPC/partner-service/pull/20) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#19](http://githubdev.dco.elmae/EPC/partner-service/pull/19) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#18](http://githubdev.dco.elmae/EPC/partner-service/pull/18) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#17](http://githubdev.dco.elmae/EPC/partner-service/pull/17) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#16](http://githubdev.dco.elmae/EPC/partner-service/pull/16) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#15](http://githubdev.dco.elmae/EPC/partner-service/pull/15) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#14](http://githubdev.dco.elmae/EPC/partner-service/pull/14) ([bappala](http://githubdev.dco.elmae/bappala))
- EPC-9782:added new line for active [\#13](http://githubdev.dco.elmae/EPC/partner-service/pull/13) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#12](http://githubdev.dco.elmae/EPC/partner-service/pull/12) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#11](http://githubdev.dco.elmae/EPC/partner-service/pull/11) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 [\#10](http://githubdev.dco.elmae/EPC/partner-service/pull/10) ([bappala](http://githubdev.dco.elmae/bappala))
- changing version from v1 to v2 and session to origin [\#9](http://githubdev.dco.elmae/EPC/partner-service/pull/9) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc manifest [\#8](http://githubdev.dco.elmae/EPC/partner-service/pull/8) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9782 1 [\#7](http://githubdev.dco.elmae/EPC/partner-service/pull/7) ([bappala](http://githubdev.dco.elmae/bappala))
- modified application.yml file [\#6](http://githubdev.dco.elmae/EPC/partner-service/pull/6) ([bappala](http://githubdev.dco.elmae/bappala))
- Epc 9784 [\#5](http://githubdev.dco.elmae/EPC/partner-service/pull/5) ([bappala](http://githubdev.dco.elmae/bappala))
- moved application.yml to resources and removed unwanted dependency fr… [\#4](http://githubdev.dco.elmae/EPC/partner-service/pull/4) ([bappala](http://githubdev.dco.elmae/bappala))
- IP Range and ECS TF scripts [\#3](http://githubdev.dco.elmae/EPC/partner-service/pull/3) ([bappala](http://githubdev.dco.elmae/bappala))
- adding script files and terraform microservice infra [\#2](http://githubdev.dco.elmae/EPC/partner-service/pull/2) ([bappala](http://githubdev.dco.elmae/bappala))
- Adding deployment scripts [\#1](http://githubdev.dco.elmae/EPC/partner-service/pull/1) ([bappala](http://githubdev.dco.elmae/bappala))



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
