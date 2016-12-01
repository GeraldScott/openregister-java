package uk.gov.register.core;

import org.junit.Test;

import java.net.URI;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UriTemplateLinkResolverTest {

    private final UriTemplateLinkResolver localResolver = new UriTemplateLinkResolver(() -> "http", () -> "openregister.dev:8080");
    private final UriTemplateLinkResolver prodResolver = new UriTemplateLinkResolver(() -> "https", () -> "register.gov.uk");

    @Test
    public void linkValueReturnsLink() {
        LinkValue linkValue = new LinkValue("address", "don't care", "don't care", "1111112");

        assertThat(localResolver.resolve(linkValue), is(URI.create("http://address.openregister.dev:8080/record/1111112")));
        assertThat(prodResolver.resolve(linkValue), is(URI.create("https://address.register.gov.uk/record/1111112")));
    }

    @Test
    public void curieValueReturnsCorrectLink() {
        LinkValue.CurieValue charityCurieValue = new LinkValue.CurieValue("charity:123456", "don't care", "don't care");
        LinkValue.CurieValue companyCurieValue = new LinkValue.CurieValue("company:654321", "don't care", "don't care");

        assertThat(localResolver.resolve(charityCurieValue), is(URI.create("http://charity.openregister.dev:8080/record/123456")));
        assertThat(localResolver.resolve(companyCurieValue), is(URI.create("http://company.openregister.dev:8080/record/654321")));
    }

}
