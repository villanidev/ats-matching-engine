package com.villanidev.atsmatchingengine.templates;

import com.villanidev.atsmatchingengine.domain.CvGenerated;

public interface CvTemplate {
    String renderMarkdown(CvGenerated cvGenerated);
}
