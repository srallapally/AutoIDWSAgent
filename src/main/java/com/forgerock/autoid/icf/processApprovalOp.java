package com.forgerock.autoid.icf;
import org.identityconnectors.framework.api.operations.APIOperation;

public interface processApprovalOp extends APIOperation {
    public void handleApprovals(String workItemId);
}
