package com.proftaak.pts4.rest.response.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Michon on 22-6-2015.
 */
public class Metadata {
    /**
     * The pagination metadata
     */
    private PaginationMetadata paginationMetadata;

    /**
     * The warnings
     */
    private List<String> warnings = new ArrayList<>();

    public PaginationMetadata getPaginationMetadata() {
        return paginationMetadata;
    }

    public void setPaginationMetadata(PaginationMetadata paginationMetadata) {
        this.paginationMetadata = paginationMetadata;
    }

    public void addWarning(String message) {
        this.warnings.add(message);
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        if (this.paginationMetadata != null) {
            data.put("pagination", this.paginationMetadata);
        }
        data.put("warnings", this.warnings);
        return data;
    }
}
