package org.develop.services;

import org.develop.commons.model.mainUse.Funko;
import org.develop.commons.model.mainUse.Modelo;
import org.develop.commons.model.mainUse.Notificacion;
import org.develop.repositories.funkos.FunkoRepository;
import org.develop.services.files.BackupManagerImpl;
import org.develop.services.funkos.FunkoNotification;
import org.develop.services.funkos.FunkoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FunkoServiceImplTest {

    private Funko funko1, funko2;

    @Mock
    FunkoRepository repository;
    @Mock
    FunkoNotification funkoNotification;
    @Mock
    BackupManagerImpl backupManager;

    @InjectMocks
    FunkoServiceImpl service;

    @BeforeEach
    void setup(){

        funko1 = Funko.builder()
                .id(1)
                .uuid(UUID.randomUUID())
                .name("test")
                .modelo(Modelo.OTROS)
                .precio(1.0)
                .fecha_lanzamiento(LocalDate.of(2024,1,20))
                .build();

        funko2 = Funko.builder()
                .id(2)
                .uuid(UUID.randomUUID())
                .name("test2")
                .modelo(Modelo.MARVEL)
                .precio(1.5)
                .fecha_lanzamiento(LocalDate.of(2026,4,10))
                .build();
    }
    @Test
    void findAll() {
        var listFunk = List.of(funko1,funko2);

        when(repository.findAll()).thenReturn(Flux.fromIterable(listFunk));

        var res = service.findAll().collectList().block();

        assertAll(
                ()-> assertFalse(res.isEmpty()),
                ()-> assertEquals(2,res.size()),
                ()-> assertEquals(res.get(0).getUuid(),funko1.getUuid()),
                ()-> assertEquals(res.get(1).getUuid(),funko2.getUuid())
        );
        verify(repository,times(1)).findAll();
    }

    @Test
    void findById() {
        when(repository.findById(1)).thenReturn(Mono.just(funko1));

        var res = service.findById(1).blockOptional();

        assertAll(
                ()-> assertTrue(res.isPresent()),
                ()-> assertEquals(res.get().getName(),funko1.getName()),
                ()-> assertEquals(res.get().getUuid(),funko1.getUuid())
        );

        verify(repository,times(1)).findById(1);
    }

    @Test
    void findByIdError() {

        when(repository.findById(1)).thenReturn(Mono.empty());

        var res = assertThrows(Exception.class, ()->service.findById(1).blockOptional());

        assertTrue(res.getMessage().contains("Funko with id 1 not found"));

        verify(repository, times(1)).findById(1);
    }

    @Test
    void findByName() {
        var listFunk = List.of(funko1,funko2);

        when(repository.findByName("test")).thenReturn(Flux.fromIterable(listFunk));

        var res = service.findByName("test").collectList().block();

        assertAll(
                ()-> assertFalse(res.isEmpty()),
                ()-> assertEquals(2,res.size()),
                ()-> assertEquals(res.get(0).getUuid(),funko1.getUuid()),
                ()-> assertEquals(res.get(1).getUuid(),funko2.getUuid())
        );

        verify(repository,times(1)).findByName("test");
    }

    @Test
    void save() {
        when(repository.findByUuid(funko1.getUuid())).thenReturn(Mono.just(funko1));
        when(repository.save(funko1)).thenReturn(Mono.just(funko1));

        var res = service.saveWithOutNotification(funko1).block();

        assertAll(
                ()-> assertNotNull(res),
                ()-> assertEquals(res.getName(),funko1.getName()),
                ()-> assertEquals(res.getUuid(),funko1.getUuid())
        );

        verify(repository,times(1)).save(funko1);
    }

    @Test
    void saveNoti(){
        when(repository.findByUuid(funko1.getUuid())).thenReturn(Mono.just(funko1));
        when(repository.save(funko1)).thenReturn(Mono.just(funko1));
        doNothing().when(funkoNotification).notify(any());

        Funko res = service.save(funko1).block();

        assertAll(
                ()-> assertNotNull(res),
                ()-> assertEquals(res.getName(),funko1.getName()),
                ()-> assertEquals(res.getUuid(),funko1.getUuid())
        );
        verify(repository,times(1)).save(funko1);
        verify(repository,times(1)).findByUuid(funko1.getUuid());
    }
    @Test
    void update() {
        when(repository.findById(1)).thenReturn(Mono.just(funko1));
        when(repository.update(funko1)).thenReturn(Mono.just(funko1));

        var res = service.updateWithOutNotification(funko1).block();

        assertAll(
                ()-> assertEquals(res.getName(),funko1.getName()),
                ()-> assertEquals(res.getUuid(),funko1.getUuid())
        );
        verify(repository,times(1)).update(funko1);
        verify(repository,times(1)).findById(1);
    }

    @Test
    void updateNoti() {
        when(repository.findById(1)).thenReturn(Mono.just(funko1));
        when(repository.update(funko1)).thenReturn(Mono.just(funko1));

        var res = service.update(funko1).block();

        assertAll(
                ()-> assertEquals(res.getName(),funko1.getName()),
                ()-> assertEquals(res.getUuid(),funko1.getUuid())
        );
    }

    @Test
    void deleteById() {
        when(repository.deleteById(1)).thenReturn(Mono.just(true));
        when(repository.findById(1)).thenReturn(Mono.just(funko1));
        var res = service.deleteByIdWithOutNotification(1).block();

        assertEquals(res,funko1);

        verify(repository,times(1)).deleteById(1);
    }

    @Test
    void deleteByIdNoti() {
        when(repository.deleteById(1)).thenReturn(Mono.just(true));
        when(repository.findById(1)).thenReturn(Mono.just(funko1));
        var res = service.deleteById(1).block();

        assertEquals(res,funko1);

        verify(repository,times(1)).deleteById(1);
        verify(repository,times(1)).findById(1);
    }
    @Test
    void deletedByIdError(){

        when(repository.findById(1)).thenReturn(Mono.empty());

        var res = assertThrows(Exception.class, ()->service.deleteById(1).blockOptional());

        assertTrue(res.getMessage().contains("Funko with id 1 not found"));

        verify(repository, times(1)).findById(1);
    }

    @Test
    void deleteAll() {
        when(repository.deleteAll()).thenReturn(Mono.empty());

        service.deleteAll().block();

        verify(repository,times(1)).deleteAll();
    }

    @Test
    void findByUuid(){
        when(repository.findByUuid(funko1.getUuid())).thenReturn(Mono.just(funko1));

        var res = service.findByUuid(funko1.getUuid()).block();

        assertAll(
                ()-> assertNotNull(res),
                ()-> assertEquals(res.getUuid(),funko1.getUuid())
        );

        verify(repository,times(1)).findByUuid(funko1.getUuid());
    }

    @Test
    void getNotification(){
        Notificacion<Funko> funkoNot = new Notificacion<>(Notificacion.Tipo.NEW,funko1);
        when(funkoNotification.getNotificationAsFlux()).thenReturn(Flux.just(funkoNot));

        var res = service.getNotifications().collectList().block();

        assertAll(
                ()-> assertFalse(res.isEmpty()),
                ()-> assertEquals(1,res.size()),
                ()-> assertEquals(res.get(0),funkoNot)
        );

        verify(funkoNotification,times(1)).getNotificationAsFlux();
    }
    @Test
    void backup() {
        var listFunk = List.of(funko1,funko2);

        when(backupManager.writeFile("funkosTest.json",listFunk)).thenReturn(Mono.just(true));
        when(repository.findAll()).thenReturn(Flux.fromIterable(listFunk));

        var res = service.backup("funkosTest.json").block();

        assertTrue(res);
    }

    @Test
    void imported() {
        var listFunk = List.of(funko1,funko2);

        when(backupManager.readFile("funkosTest.json")).thenReturn(Flux.fromIterable(listFunk));

        var res = service.imported("funkosTest.json").collectList().block();

        assertAll(
                ()-> assertNotNull(res),
                ()-> assertEquals(2,res.size())
        );

    }
}