/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.session;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.text.TextUtils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class SessionTest {
    private static final String TEST_URL = "https://www.mozilla.org";
    private static final String TEST_URL_2 = "https://www.example.org";

    @Test
    public void testEmptySession() {
        final Session session = new Session(Source.VIEW, TEST_URL);

        assertFalse(TextUtils.isEmpty(session.getUUID()));
        assertEquals(TEST_URL, session.getUrl().getValue());

        assertEquals(0, (int) session.getProgress().getValue());
        assertFalse(session.getSecure().getValue());
        assertFalse(session.getLoading().getValue());
    }

    @Test
    public void testObserversGetInitialValues() {
        final Session session = new Session(Source.VIEW, TEST_URL);

        {
            final Observer<String> urlObserver = mockObserver();
            session.getUrl().observe(mockLifecycleOwner(), urlObserver);
            verify(urlObserver).onChanged(TEST_URL);
        }
        {
            final Observer<Integer> progressObserver = mockObserver();
            session.getProgress().observe(mockLifecycleOwner(), progressObserver);
            verify(progressObserver).onChanged(0);
        }
        {
            final Observer<Boolean> secureObserver = mockObserver();
            session.getSecure().observe(mockLifecycleOwner(), secureObserver);
            verify(secureObserver).onChanged(false);
        }
        {
            final Observer<Boolean> loadingObserver = mockObserver();
            session.getLoading().observe(mockLifecycleOwner(), loadingObserver);
            verify(loadingObserver).onChanged(false);
        }
    }

    @Test
    public void testObservingValueChanges() {
        final Session session = new Session(Source.VIEW, TEST_URL);

        {
            final Observer<String> urlObserver = mockObserver();
            session.getUrl().observe(mockLifecycleOwner(), urlObserver);
            verify(urlObserver).onChanged(TEST_URL);

            session.setUrl(TEST_URL_2);
            verify(urlObserver).onChanged(TEST_URL_2);
        }
        {
            final Observer<Integer> progressObserver = mockObserver();
            session.getProgress().observe(mockLifecycleOwner(), progressObserver);
            verify(progressObserver).onChanged(0);

            session.setProgress(42);
            verify(progressObserver).onChanged(42);
        }
        {
            final Observer<Boolean> secureObserver = mockObserver();
            session.getSecure().observe(mockLifecycleOwner(), secureObserver);
            verify(secureObserver).onChanged(false);

            session.setSecure(true);
            verify(secureObserver).onChanged(true);
        }
        {
            final Observer<Boolean> loadingObserver = mockObserver();
            session.getLoading().observe(mockLifecycleOwner(), loadingObserver);
            verify(loadingObserver).onChanged(false);

            session.setLoading(true);
            verify(loadingObserver).onChanged(true);
        }
    }

    @Test
    public void testSavingAndRetrievingWebViewState() {
        final Session session = new Session(Source.VIEW, TEST_URL);

        assertNull(session.getWebViewState());

        {
            final Bundle bundle = new Bundle();
            bundle.putInt("friday", 13);

            session.saveWebViewState(bundle);
        }

        assertNotNull(session.getWebViewState());

        {
            final Bundle bundle = session.getWebViewState();
            assertTrue(bundle.containsKey("friday"));
            assertEquals(13, bundle.getInt("friday"));
        }
    }

    @Test
    public void testIsRecorded() {
        final Session session = new Session(Source.VIEW, TEST_URL);
        assertFalse(session.isRecorded());

        session.markAsRecorded();
        assertTrue(session.isRecorded());
    }

    @Test
    public void testSameAs() {
        final Session session1 = new Session(Source.VIEW, TEST_URL);
        final Session session2 = new Session(Source.VIEW, TEST_URL);

        assertFalse(session1.isSameAs(session2));
        assertFalse(session2.isSameAs(session1));

        assertTrue(session1.isSameAs(session1));
        assertTrue(session2.isSameAs(session2));
    }

    @SuppressWarnings("unchecked")
    private <T> Observer<T> mockObserver() {
        return mock(Observer.class);
    }

    private LifecycleOwner mockLifecycleOwner() {
        final LifecycleOwner lifecycleOwner = mock(LifecycleOwner.class);

        LifecycleRegistry registry = new LifecycleRegistry(lifecycleOwner);
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);

        doReturn(registry).when(lifecycleOwner).getLifecycle();

        return lifecycleOwner;
    }
}