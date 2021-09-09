package org.qortal.block;

import org.qortal.naming.Name;
import org.qortal.repository.DataException;

/**
 * Block 541334
 * <p>
 * This block had the same problem as block 535658 and 536140.
 * <p>
 * Original transaction:
 * <code><pre>
 {
     "type": "REGISTER_NAME",
     "timestamp": 1630526683337,
     "reference": "FrNXL9JRSXkbsKBu7Zf9YRnNBxUQBqYCnim5HTBsM5upj6PV7rQLYmXtrpb8Ds94qmdQTXTCRsJ6FFzGrXCzj5S",
     "fee": "0.00100000",
     "signature": "3GLuWs7Xg4m4ZMtu1LKbetfLqNSmp8CJ28tHYPksNr2t96GdPyURT2af9PL2bBSZgbP3vhGYcWgEGoda3mb5vzUQ",
     "txGroupId": 0,
     "blockHeight": 532885,
     "approvalStatus": "NOT_REQUIRED",
     "creatorAddress": "QRxuXPLfz7g5AA3STmCP199K1QdYx6E6Rr",
     "registrantPublicKey": "8HkfwVL4iNgpkjddxmDKi5t5Bn3c4RsARE5WoxgXg5fd",
     "name": "Qithub",
     "data": "{\"age\":30}"
 }
 </pre></code>
 * <p>
 * Duplicate transaction:
 * <code><pre>
 {
     "type": "REGISTER_NAME",
     "timestamp": 1631179021899,
     "reference": "3GLuWs7Xg4m4ZMtu1LKbetfLqNSmp8CJ28tHYPksNr2t96GdPyURT2af9PL2bBSZgbP3vhGYcWgEGoda3mb5vzUQ",
     "fee": "0.00100000",
     "signature": "5tUa5uxo4iALHmVMBjLcHxepjarK38PMyL7XXKAVBWQpTgASfDVk6yYfs4GLn9j4qbbpERhq1h5dAx49vCPrhGtB",
     "txGroupId": 0,
     "blockHeight": 541334,
     "approvalStatus": "NOT_REQUIRED",
     "creatorAddress": "QRxuXPLfz7g5AA3STmCP199K1QdYx6E6Rr",
     "registrantPublicKey": "8HkfwVL4iNgpkjddxmDKi5t5Bn3c4RsARE5WoxgXg5fd",
     "name": "Qithub",
     "data": "Registered Name on the Qortal Chain"
 }
 </pre></code>
 */
public final class Block541334 {

	private Block541334() {
		/* Do not instantiate */
	}

	public static void processFix(Block block) throws DataException {
		// Unregister the existing name record if it exists
		// This ensures that the duplicate name is considered valid, and therefore
		// the second (i.e. duplicate) REGISTER_NAME transaction data is applied.
		// Both were issued by the same user account, so there is no conflict.
		Name name = new Name(block.repository, "Qithub");
		name.unregister();
	}

}