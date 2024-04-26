SET SCHEMA 'partnerschema';

-- DO NOT Run this in prod
UPDATE partnerschema.product
SET access_entitlements = '{"allow": "^$", "deny": "^$"}', status = 'deprecated', updated_by='urn:elli:service:epc-partner-service'
	WHERE id NOT IN (SELECT id from partnerschema.product WHERE
	(partner_id='007001' AND name='AppraisalRI_Automation') OR
	(partner_id='007001' AND name='Appraisal-RI') OR
	(partner_id='007001' AND name='ProductandPricingOptimalBlue-QATest') or
	(partner_id='007001' AND name='finicity-services') or
	(partner_id='007001' AND name='01-Flood-DataVerifyCBC') or
	(partner_id='007001' AND name='A_QA_TEST_SSF_Ashwani_Product') or
	(partner_id='007001' AND name='ssf-showcase21') or
	(partner_id='007001' AND name='ssf-showcase') OR
	(partner_id='007001' AND name='flood-SLP-test-product') OR
	(partner_id='007001' AND name='form-free-services') OR
	(partner_id='007001' AND name='02-Flood-DataVerifyCBC') OR
	(partner_id='007001' AND name='finicity-voe-services') OR
	(partner_id='007001' AND name='finicity-voa-services') OR
	(partner_id='007001' AND name='ecc-epc2-voa-platform-automation') OR 
	(partner_id='007001' AND name='finicity-voa-services-test') OR 
	(partner_id='007001' AND name='finicity-voe-services-test') OR 
	(partner_id='007001' AND name='finicity-voia-services-test') OR
	(partner_id='007001' AND name='ecc-epc2-voe-platform-automation') OR 
	(partner_id='007001' AND name like 'EPPS2.0.%') OR
	(partner_id='007001' AND name='gdms_services_1') OR 
	(partner_id='007001' AND name='gdms_services_2') OR 
	(partner_id='007001' AND name='gdms_services_test') OR 
	(partner_id='007001' AND name='gdms_services') OR
	(partner_id='007001' AND name='finicity-ecc-mock-voa-service') OR
	(partner_id='007001' AND name='finicity-ecc-mock-voie-service') OR
	(partner_id='007001' AND name='FirstAmericanTitle_EPC-INT-1') OR
	(partner_id='007001' AND name='FirstAmericanTitle_EPC-INT-2') OR
	(partner_id='007001' AND name='SmartFeesByClosingCorp') OR
	(partner_id='007001' AND name='Flood-DataVerifyCBC-QAA') OR
  	(partner_id='007001' AND name='01-Flood-DataVerifyCBC') OR
 	(partner_id='007001' AND name='02-Flood-DataVerifyCBC') OR
 	(partner_id='007002' AND name='form-free-services') OR
  	(partner_id='007002' AND name='formfree-ecc-mock-voa-service') OR
  	(partner_id='007003' AND name='generic-ecc-mock-voie-service') OR
  	(partner_id='007003' AND name='generic-ecc-mock-voa-service') OR
  	(partner_id='007004' AND name='DataDocsTest_INVEPC2') OR
  	(partner_id='007010' AND name='Brent-TestPrd') OR
  	(partner_id='307827' AND name='Credit-Equifax') OR
  	(partner_id='274365' AND name='Flood-CoreLogic') OR
  	(partner_id='274365' AND name='Credit-CoreLogic') OR
 	(partner_id='274365' AND name='Credit-CoreLogic(DigiCert)') OR
 	(partner_id='304369' AND name='Credit-CBCFD') OR
 	(partner_id='303815' AND name='Credit-CreditPlus') OR
 	(partner_id='311509' AND name='Credit-OldRepublic') OR
 	(partner_id='306025' AND name='Credit-InformativeResearch') OR
 	(partner_id='306113' AND name='Credit-CBCTAvantus') OR
  	(partner_id='11114326' AND name='ProductandPricingOptimalBlue') OR
	(partner_id='10000176' AND name='Flood-ServiceLink') OR
	(partner_id='10000034' AND name='AUS-EC-PU') OR
	(partner_id='10000035' AND name='AUS-LQA-PU') OR
	(partner_id='11120203' AND name='Credit-MeridianLink') OR 
	(partner_id='11125641' AND name='01-Verif-Equifax Work Number') OR
	(partner_id='11125641' AND name='02-Verif-Equifax Work Number') OR 
	(partner_id='11213781' AND name='Credit-TransUnion') OR 
	(partner_id='11133173' AND name='essent-mi-services-test') OR
	(partner_id='11120198' AND name='FirstAmericanTitle-INT') OR 
	(partner_id='11120198' AND name='FirstAmericanTitle-INTTest') OR 
	(partner_id='11229337' AND name like '%Geico%') OR
	(partner_id='11168799' AND name like '%Loanbeam%') OR
	(partner_id='11125641' AND name like '%Equifax%') OR
	(partner_id='313568' AND name like '%mi%') OR
	(partner_id='11133173' AND name like '%mi%') OR
	(partner_id='274350' AND name like '%mi%') OR
	(partner_id='274059' AND name like '%mi%') OR
	(partner_id='274895' AND name like '%mi%') OR
	(partner_id='11120198' AND name='FirstAmericanTitle-INT2'));


COMMIT;
