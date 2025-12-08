package com.example.registroflytransportation.viewModel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.registroflytransportation.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class VueloProgramadoViewModelTest {

    // Regla para que las corutinas usen el hilo de test en lugar del hilo de Android.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // Regla para componentes de arquitectura de Android.
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var application: Application

    private lateinit var viewModel: VueloProgramadoViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        `when`(application.applicationContext).thenReturn(application)
        viewModel = VueloProgramadoViewModel(application)
    }

    @Test
    fun `searchAirports con consulta corta no actualiza las sugerencias`() = runTest {
        // Preparación
        val shortQuery = "a"

        // Actuación
        viewModel.searchAirports(shortQuery, "origen")

        // Afirmación
        val currentState = viewModel.formState.first()
        assertTrue(currentState.origenSuggestions.isEmpty())
        assertFalse(currentState.isLoadingOrigen)
    }

    @Test
    fun `searchAirports con consulta válida activa el estado de carga`() = runTest {
        // Preparación
        val validQuery = "Santiago"
        val stateChanges = mutableListOf<AdminFormState>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.formState.collect { stateChanges.add(it) }
        }

        // Actuación
        viewModel.searchAirports(validQuery, "origen")

        // ¡LA CLAVE! Avanzamos el reloj virtual para saltarnos el delay(300) del ViewModel.
        advanceTimeBy(301)

        // Afirmación
        val wasLoading = stateChanges.any { it.isLoadingOrigen }
        assertTrue(
            "El estado de carga para 'origen' nunca se activó. Estados recolectados: $stateChanges",
            wasLoading
        )

        job.cancel()
    }
}
