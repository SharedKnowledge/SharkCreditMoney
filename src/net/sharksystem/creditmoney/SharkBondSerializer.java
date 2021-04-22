package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.utils.ASAPSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class SharkBondSerializer {
    // TODO
    static void writeBond(SharkBond bond, OutputStream os) throws IOException {
        // serialize bond in stream

        // serialize member variable 1. Examples
        ASAPSerialization.writeIntegerParameter(42, os);
    }

    static SharkBond readBond(InputStream is) throws IOException, ASAPException {
        // read all member

        // example
        int intMember = ASAPSerialization.readIntegerParameter(is);

        // finally create Bond object wth all it parameter and return
        return new InMemoSharkBond(null, null, null, 0);
    }
}
