package org.ujorm.kotlin.core;

import io.mockk.junit5.MockKExtension;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ujorm.kotlin.core.model.TestEntityModelImpl;

@ExtendWith(MockKExtension.class)
class AbstractEntityJavaTest {

    @Test
    void rawEntityTest() {
        var rawEntity = new DummyEntity();
        if (rawEntity.___$() == null) {
            throw new IllegalStateException("Test exeption");
        }
    }

    class DummyEntity implements AbstractEntity<String> {
        @NotNull
        @Override
        public RawEntity<String> ___$() {
            return new RawEntity<String>(new TestEntityModelImpl());
        }
    }
}