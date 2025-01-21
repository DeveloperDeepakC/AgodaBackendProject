package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.HotelDto;
import com.deepak.projects.airBnbApp.dto.HotelInfoDto;
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

import static com.deepak.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating new hotel with name: {}", hotelDto.getName());
        Hotel hotel= modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        hotel.setOwner(user);

        hotel= hotelRepository.save(hotel);
        log.info("Created a new hotel with id: {}", hotelDto.getId());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Getting hotel with id: {}", id);

        Hotel hotel= hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+id));

//        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//        if(!user.equals(hotel.getOwner())){
//            throw new UnAuthorisedException("This user doesnt not own this hotel with id: "+id);
//        }
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel with id: {}", id);
        Hotel hotel= hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+id));

        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn not own this hotel with id: "+id);
        }

        modelMapper.map(hotelDto,hotel);
        hotel.setId(id);
        hotel= hotelRepository.save(hotel);
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        //delete the future inventories for this hotel
        Hotel hotel= hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+id));

        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn not own this hotel with id: "+id);
        }

        for(Room room: hotel.getRooms()){
            inventoryService.deleteAllInventories(room);
            roomRepository.deleteById(room.getId());
        }
        hotelRepository.deleteById(id);

    }

    @Override
    @Transactional
    public void activateHotel(Long id) {
        log.info("Updating hotel with id: {}", id);
        Hotel hotel= hotelRepository
                .findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+id));

        User user= (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!user.equals(hotel.getOwner())){
            throw new UnAuthorisedException("This user doesn not own this hotel with id: "+id);
        }

        hotel.setActive(true);
        //Create inventories for this hotel for all rooms(assuming only do it once)
        for(Room room: hotel.getRooms()){
            inventoryService.initializeRoomForAYear(room);
        }
    }

    //public method
    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        Hotel hotel= hotelRepository
                .findById(hotelId)
                .orElseThrow(()-> new ResourceNotFoundException("Hotel not found with id: "+hotelId));

        List<RoomDto> rooms= hotel.getRooms()
                .stream()
                .map(room -> modelMapper.map(room,RoomDto.class))
                .toList();

        return new HotelInfoDto(modelMapper.map(hotel,HotelDto.class),rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        log.info("Getting all hotels for the admin user with id: {}", getCurrentUser().getId());
        User user= getCurrentUser();
        List<Hotel> hotels= hotelRepository.findByOwner(user);

        return hotels
                .stream()
                .map(hotel -> modelMapper.map(hotel,HotelDto.class))
                .toList();
    }
}
