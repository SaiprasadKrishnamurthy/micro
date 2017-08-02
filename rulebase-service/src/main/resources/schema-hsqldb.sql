CREATE TABLE IF NOT EXISTS RuleDefs(
   name VARCHAR(80) PRIMARY KEY
  ,description      VARCHAR(81) NOT NULL
  ,family     VARCHAR(30) NOT NULL
  ,evaluationCondition      VARCHAR(400) NOT NULL
  ,executionAction      VARCHAR(400) NOT NULL
  ,active    VARCHAR(1) NOT NULL
  ,priority    INTEGER NOT NULL
  ,shortCircuit    VARCHAR(1) NOT NULL
);

CREATE INDEX RuleDefs_Family ON RuleDefs (family);
CREATE INDEX RuleDefs_Active ON RuleDefs (active);

