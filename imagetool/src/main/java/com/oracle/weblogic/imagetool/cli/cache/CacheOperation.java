// Copyright (c) 2019, 2020, Oracle Corporation and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.

package com.oracle.weblogic.imagetool.cli.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.oracle.weblogic.imagetool.api.model.CommandResponse;
import com.oracle.weblogic.imagetool.cachestore.CacheStore;
import com.oracle.weblogic.imagetool.cachestore.CacheStoreFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Unmatched;

public abstract class CacheOperation implements Callable<CommandResponse> {

    protected CacheStore cacheStore = new CacheStoreFactory().get();

    @Unmatched
    List<String> unmatchedOptions = new ArrayList<>();

    @Spec
    CommandSpec spec;
}
