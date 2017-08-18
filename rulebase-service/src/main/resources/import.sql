INSERT INTO Rule(Name,description,family,evaluationCondition,executionAction,active, priority, shortCircuit, abortOnError) VALUES ('CountryOfBirthCheckRule','Check the Country of Birth','RISK_RULE','payload[''cob''] == ''GB''' ,'#recordMatch(#ctx)', 'Y', 1, 'N', 'Y');
INSERT INTO Rule(Name,description,family,evaluationCondition,executionAction,active, priority, shortCircuit, abortOnError) VALUES ('NationalityCheckRule','Check the Nationality','RISK_RULE','payload[''nationality''] == ''IN''' ,'#recordMatch(#ctx)', 'Y', 2, 'N', 'Y');
INSERT INTO Rule(Name,description,family,evaluationCondition,executionAction,active, priority, shortCircuit, abortOnError) VALUES ('EventTypeAndSubTypeCheckRule','Check the event type and subtype and perform the risk assessment','RISK_RULE','payload[''eventType''] == ''MOVEMENT_EVENT'' && payload[''eventSubtype''] == ''BOOKING''' ,'#initiateRiskAssessment(#ctx, {''WL1'', ''WL2''}, {''PRF1''})', 'Y', 3, 'N', 'Y');
INSERT INTO Rule(Name,description,family,evaluationCondition,executionAction,active, priority, shortCircuit, abortOnError) VALUES ('GenderCheckRule','Gender Check Rule','RISK_RULE','payload[''gender''] == ''M''' ,'#recordMatch(#ctx)', 'Y', 4, 'N', 'Y');
