package org.stzverev.cardcostapi.util.resourcereader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
public class ResourceReader {

    private final ObjectMapper objectMapper;

    public ResourceReader(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResourceReaderItem from(final String path) {
        return new ResourceReaderItem(path, objectMapper);
    }

    public static class ResourceReaderItem {

        private final String path;

        private final ObjectMapper mapper;

        private ResourceReaderItem(final String path, final ObjectMapper mapper) {
            this.path = path;
            this.mapper = mapper;
        }

        public <T> T mapTo(final Class<T> clazz) {
            return applyForInputStream(inputStream -> mapper.readValue(inputStream, clazz));
        }

        @SuppressWarnings("unused")
        public String readAsString() {
            return readAsString(StandardCharsets.UTF_8);
        }

        public byte[] readAsBytes() {
            return applyForInputStream(InputStream::readAllBytes);
        }

        public String readAsString(Charset charset) {
            return new String(readAsBytes(), charset);
        }

        public <R> R applyForInputStream(FunctionThrowable<InputStream, R> function) {
            try (final var inputStream = getAsStream()) {
                return function.applyWrap(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @SneakyThrows
        public InputStream getAsStream() {
            return getResource().getInputStream();
        }

        public @NotNull ClassPathResource getResource() {
            return new ClassPathResource(path);
        }

        public interface FunctionThrowable<T, R> {

            R apply(T t) throws Exception;

            default R applyWrap(final T t) {
                try {
                    return apply(t);
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            }

        }

    }

}
