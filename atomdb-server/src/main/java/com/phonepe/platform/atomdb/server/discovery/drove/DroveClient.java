package com.phonepe.platform.atomdb.server.discovery.drove;

import com.phonepe.platform.atomdb.server.discovery.Address;
import java.util.List;

public interface DroveClient {

    List<DroveAddress> peers();
}