INSERT INTO RuleDefs(Name,description,family,evaluationCondition,executionAction,active, priority, shortCircuit) VALUES ('NationalityCheckRule','Check the Nationality','RISK_RULE','payload[''nationality''] == ''IN''' ,'#recordMatch(#ctx)', 'Y', 1, 'N');
INSERT INTO RuleDefs(Name,description,family,evaluationCondition,executionAction,active, priority, shortCircuit) VALUES ('EventTypeAndSubTypeCheckRule','Check the event type and subtype and perform the risk assessment','RISK_RULE','payload[''eventType''] == ''MOVEMENT_EVENT'' && payload[''eventSubtype''] == ''BOOKING''' ,'#initiateRiskAssessment(#ctx, {''WL1'', ''WL2''}, {''PRF1''})', 'Y', 2, 'N');
