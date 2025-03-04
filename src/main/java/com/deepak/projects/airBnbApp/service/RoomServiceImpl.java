package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.RoomDto;
import com.deepak.projects.airBnbApp.entity.Hotel;
import com.deepak.projects.airBnbApp.entity.Room;
import com.deepak.projects.airBnbApp.entity.User;
import com.deepak.projects.airBnbApp.exception.ResourceNotFoundException;
import com.deepak.projects.airBnbApp.exception.UnAuthorisedException;
import com.deepak.projects.airBnbApp.repository.HotelRepository;
import com.deepak.projects.airBnbApp.repository.RoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.deepak.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService{

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;

    @Override
    public RoomDto createNewRoom(Long hotelId, RoomDto roomDto) {
        log.info("Creating new room in hotel with id: {}", hotelId);
        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+hotelId));

        User user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn not own this hotel with id: "+hotelId);
        }

        Room room= modelMapper.map(roomDto, Room.class);
        room.setHotel(hotel);
        room= roomRepository.save(room);

        //Create inventory as soon as room is created and hotel is active
        if(hotel.getActive()){
            inventoryService.initializeRoomForAYear(room);
        }
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    public List<RoomDto> getAllRoomsInHotel(Long hotelId) {
        log.info("Getting all rooms in hotel with id: {}", hotelId);
        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+hotelId));
        return hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room,RoomDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public RoomDto getRoomById(Long roomId) {
        log.info("Getting room with id: {}", roomId);
        Room room= roomRepository
                .findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: "+roomId));
        return modelMapper.map(room,RoomDto.class);
    }

    @Override
    @Transactional
    public void deleteRoomById(Long roomId) {
        log.info("Deleting room with id: {}", roomId);
        Room room= roomRepository
                .findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: "+roomId));

        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(room.getHotel().getOwner())){
            throw new UnAuthorisedException("This user doesnt not own this room  with id: "+roomId);
        }

        //delete the future inventories for this room
        inventoryService.deleteAllInventories(room);
        roomRepository.deleteById(roomId);
    }

    @Override
    @Transactional
    public RoomDto updateRoomById(Long hotelId, Long roomId, RoomDto roomDto) {
        log.info("Updating room with id: {}", roomId);
        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+hotelId));

        User user= getCurrentUser();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user does not own this hotel with id: "+hotelId);
        }

        Room room= roomRepository
                .findById(roomId)
                .orElseThrow(()-> new ResourceNotFoundException("Room not found with id: "+roomId));
        modelMapper.map(roomDto, room);
        room.setId(roomId);

        //TODO: If price/inventory is updated, update the inventories as well
        room=roomRepository.save(room);
        return modelMapper.map(room,RoomDto.class);
    }
}
