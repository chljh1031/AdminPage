package com.fastcampus.java.service;

import com.fastcampus.java.ifs.CrudInterface;
import com.fastcampus.java.model.entity.OrderGroup;
import com.fastcampus.java.model.entity.User;
import com.fastcampus.java.model.enumclass.UserStatus;
import com.fastcampus.java.model.network.Header;
import com.fastcampus.java.model.network.Pagination;
import com.fastcampus.java.model.network.request.UserApiRequest;
import com.fastcampus.java.model.network.response.ItemApiResponse;
import com.fastcampus.java.model.network.response.OrderGroupApiResponse;
import com.fastcampus.java.model.network.response.UserApiResponse;
import com.fastcampus.java.model.network.response.UserOrderInfoApiResponse;
import com.fastcampus.java.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserApiLogicService implements CrudInterface<UserApiRequest, UserApiResponse> {

    private final UserRepository userRepository;
    private final OrderGroupApiLogicService orderGroupApiLogicService;
    private final ItemApiLogicService itemApiLogicService;


    // 1. request data 가져오기
    // 2. user 생성
    // 3. 생성된 데이터 기준 -> UserApiResponse return

    @Override
    public Header<UserApiResponse> create(Header<UserApiRequest> request) {

        // 1. request data
        UserApiRequest userApiRequest = request.getData();

        // 2. user 생성
        User user = User.builder()
                .account(userApiRequest.getAccount())
                .password(userApiRequest.getPassword())
                .status(UserStatus.REGISTERED)
                .phoneNumber(userApiRequest.getPhoneNumber())
                .email(userApiRequest.getEmail())
                .registeredAt(LocalDateTime.now())
                .build();
        User newUser = userRepository.save(user);

        // 3. 생성된 데이터 -> userApiResponse return
//        return response(newUser); // 페이징 전 원본
        return Header.OK(response(newUser));
    }

    @Override
    public Header<UserApiResponse> read(Long id) {

        // id -> repository getOne, getById
        // user -> userApiResponse return
        return userRepository.findById(id)
                .map(user -> response(user))
//                .map(userApiResponse -> Header.OK(userApiResponse))// 1. response 반환된 값을 Header로 한번 더 감싸줌
                .map(Header::OK)// 2. 1과 동일한 결과
//                .orElseGet(
//                        ()->Header.ERROR("데이터 없음")
//                );
                .orElseThrow(()-> new RuntimeException("데이터가 없습니다."));
    }

    @Override
    public Header<UserApiResponse> update(Header<UserApiRequest> request) {

        // 1. data 가져오기
        UserApiRequest userApiRequest = request.getData();

        // 2. id -> user 데이터를 찾고
        Optional<User> optional = userRepository.findById(userApiRequest.getId());

        return optional.map(user -> {
            // 3. data -> update
            user.setAccount(userApiRequest.getAccount()) // User 객체의 @Accessors(chain = true)를 통해 chain형식으로 이어갈 수 있음
                    .setPassword(userApiRequest.getPassword())
                    .setStatus(userApiRequest.getStatus())
                    .setPhoneNumber(userApiRequest.getPhoneNumber())
                    .setEmail(userApiRequest.getEmail())
                    .setRegisteredAt(userApiRequest.getRegisteredAt())
                    .setUnregisteredAt(userApiRequest.getUnregisteredAt());
            return user;
        })
        .map(user -> userRepository.save(user))     // update -> newUser객체 반환
        .map(updateUser -> response(updateUser))    // userApiResponse 반환
        .map(Header::OK) // 페이징 추가        
        .orElseGet(()->Header.ERROR("데이터 없음"));
    }

    @Override
    public Header delete(Long id) {
        // 1. id -> repository -> user
        Optional<User> optional = userRepository.findById(id);
        
        // 2. repository -> delete
        return optional.map(user -> {
            userRepository.delete(user);
            return Header.OK();
        })
//        .orElseGet(()->Header.ERROR("데이터 없음"));
          .orElseThrow(()-> new RuntimeException("데이터가 없습니다."));
    }
    
    // 페이징 이전 원본
//    private Header<UserApiResponse> response(User user){
//        // user객체를 가지고 userApiResponse를 만들어서 리턴해주는 메서드
//
//        UserApiResponse userApiResponse = UserApiResponse.builder()
//                .id(user.getId())
//                .account(user.getAccount())
//                .password(user.getPassword()) // todo 암호화, 길이
//                .email(user.getEmail())
//                .phoneNumber(user.getPhoneNumber())
//                .status(user.getStatus())
//                .registeredAt(user.getRegisteredAt())
//                .unregisteredAt(user.getUnregisteredAt())
//                .build();
//
//        // Header + data return
//        return Header.OK(userApiResponse);
//    }

    private UserApiResponse response(User user){

        UserApiResponse userApiResponse = UserApiResponse.builder()
                .id(user.getId())
                .account(user.getAccount())
                .password(user.getPassword())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .registeredAt(user.getRegisteredAt())
                .unregisteredAt(user.getUnregisteredAt())
                .build();

        return userApiResponse;
    }

    public Header<List<UserApiResponse>> search(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable); // 해당 page에 들어있는 User정보들을 Page<User> 형태로 리턴
        List<UserApiResponse> userApiResponseList = users.stream()
                .map(user -> response(user))
                .collect(Collectors.toList());

        // List<UserApiResponse> 모양의 데이터를
        // Header<List<UserApiResponse>> 형태로 변경해야함

        // page정보 추가
        Pagination pagination = Pagination.builder()
                .totalPages(users.getTotalPages())                 // 전체 페이지 수
                .totalElements(users.getTotalElements())           // 전체 elements 수
                .currentPage(users.getNumber())                    // 현재 페이지
                .currentElements(users.getNumberOfElements())      // 현재 페이지에 몇개의 data가 들어있는지
                .build();


        return Header.OK(userApiResponseList,pagination);
    }

    public Header<UserOrderInfoApiResponse> orderInfo(Long id) {

        // user 1. 사용자를 찾아오고
        User user = userRepository.getOne(id);
        UserApiResponse userApiResponse = response(user);


        // orderGroup 2. 오더그룹을 가져오고
        List<OrderGroup> orderGroupList = user.getOrderGroupList();                     // 유저가 가진 오더그룹 리스트를 만들고
        List<OrderGroupApiResponse> orderGroupApiResponseList = orderGroupList.stream()
                .map(orderGroup -> {
                    OrderGroupApiResponse orderGroupApiResponse = orderGroupApiLogicService.response(orderGroup);
                    
                    // item 3. 오더그룹에 해당하는 아이템들을 찾아오고
                    List<ItemApiResponse> itemApiResponseList = orderGroup.getOrderDetailList().stream()
                            .map(detail -> detail.getItem())
                            .map(item -> itemApiLogicService.response(item))
                            .collect(Collectors.toList());

                    orderGroupApiResponse.setItemApiResponseList(itemApiResponseList);
                    return orderGroupApiResponse;
                })
                .collect(Collectors.toList());
        
        // 4. 유저에 그룹내역을 저장하고
        userApiResponse.setOrderGroupApiResponseList(orderGroupApiResponseList);

        // 5. 아이템 내역과 그룹내역이 담겨있는 userApiResponse를 빌드해서 리턴
        UserOrderInfoApiResponse userOrderInfoApiResponse = UserOrderInfoApiResponse.builder()
                .userApiResponse(userApiResponse)
                .build();

        return Header.OK(userOrderInfoApiResponse);
    }
}
