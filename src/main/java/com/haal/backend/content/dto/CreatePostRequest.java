package com.haal.backend.content.dto;

import com.haal.backend.content.entity.PostVisibility;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CreatePostRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be 3-200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10, max = 5000, message = "Content must be 10-5000 characters")
    private String content;

    private PostVisibility visibility;  // Defaults to PUBLIC

    private Double latitude;   // For geospatial indexing
    private Double longitude;
}
