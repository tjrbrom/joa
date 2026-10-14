package com.x.hhqs.joa.rt1agent.command;

import com.x.hhqs.joa.common.message.JoaMessage;
import com.x.hhqs.joa.common.rt1fwk.pojo.OssCmdResult;
import com.x.hhqs.joa.common.util.CommonConstants;

public class EaddCode {
  public static String EADD_PREFIX = "joa#rt1#eadd.rt1agent.";

  public static FailResult PARSE_INPUT_FAIL = eeeh("parseInputFail");

  public static FailResult INVALID_COMMAND_CODE = eeeh("invalidCommandCode");

  public static FailResult LOGIN_FAILURE = eeeh("loginFailure");

  public static FailResult ALREADY_LOGIN = failResult(CommonConstants.TUB, "alreadyLogin");

  public static FailResult NOT_LOGIN = eeeh("notLogin");

  public static FailResult NO_PERMISSION = eeeh("noPermission");

  public static FailResult COMMAND_NOT_SUPPORTED = eeeh("commandNotSupported");
  
  public static FailResult NEED_SECONDARY_AUTHORIZATION = seof("needSecondaryAuthorization");
  
  public static FailResult REQUIRE_ADDITIONAL_USER = seof("requireAdditionalUser");
  
  public static FailResult NO_PRIVILEGE_FOR_VAULT_AUTH = seof("noPrivilegeForVaultAuth");
  
  public static FailResult AUTHORIZED_COMMAND_MISMATCH = seof("authorizedCommandMismatch");
  
  public static FailResult NO_PENDING_AUTH_COMMAND = seof("noPendingAuthCommand");
  
  public static FailResult CHECK_AIP_COMMANDS_EXCEPTION = seof("checkAipCommandsException");
  
  public static FailResult USERNAME_OR_PASSWORD_MISSING = imp("authCredentialsMissing");
  
  public static FailResult PONNBI_CONNECT_FAIL = eeeh("ponnbiConnectFail");
  
  public static FailResult PONNBI_CONNECTION_LOST = eeeh("ponnbiConnectionLost");
  
  public static FailResult SERVICE_NOT_FOUND = eeeh("serviceNotFound");
  
  public static FailResult INVOKE_COMMAND_SERVICE_FAIL = eeeh("invokeCommandServiceFail");
  
  public static FailResult MULTIPLE_OLTID_IN_AID = failResult(CommonConstants.IIPE, "multipleOltIdInAid");
  
  private static FailResult failResult(String errorCode, String eadd) {
    return new FailResult(errorCode, eadd);
  }

  private static FailResult eeeh(String eadd) {
    return new FailResult(CommonConstants.EEEH, eadd);
  }
  
  private static FailResult seof(String eadd) {
    return new FailResult(CommonConstants.SEOF, eadd);
  }
  
  private static FailResult imp(String eadd) {
    return new FailResult(CommonConstants.IMP, eadd);
  }

  static class FailResult {
    String code;
    String eadd;

    public FailResult(String code, String eadd) {
      super();
      this.code = code;
      this.eadd = EADD_PREFIX + eadd;
    }

    public OssCmdResult toOssCmdResult(JoaMessage i18n) {
      return new OssCmdResult(false, code, i18n.getMessage(eadd));
    }

    public OssCmdResult toOssCmdResult(JoaMessage i18n, Object[] args) {
        return new OssCmdResult(false, code, i18n.getMessage(eadd, args));
    }
  }

}
