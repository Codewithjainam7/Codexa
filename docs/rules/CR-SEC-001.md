# CR-SEC-001: Hardcoded Cryptographic Secret

Flags static API keys, private keys, and passwords committed directly to version control.


### AST Pattern Detection Details

The rule inspects `BinaryExpr` nodes with operator `PLUS` where one operand is a String literal containing SQL keywords (e.g. `SELECT`, `UPDATE`, `DELETE`) and the other operand is a variable identifier.
