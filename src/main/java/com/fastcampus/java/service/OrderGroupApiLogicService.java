package com.fastcampus.java.service;

import com.fastcampus.java.ifs.CrudInterface;
import com.fastcampus.java.model.entity.Category;
import com.fastcampus.java.model.entity.OrderGroup;
import com.fastcampus.java.model.network.Header;
import com.fastcampus.java.model.network.Pagination;
import com.fastcampus.java.model.network.request.OrderGroupApiRequest;
import com.fastcampus.java.model.network.response.CategoryApiResponse;
import com.fastcampus.java.model.network.response.OrderGroupApiResponse;
import com.fastcampus.java.repository.OrderGroupRepository;
import com.fastcampus.java.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderGroupApiLogicService implements CrudInterface<OrderGroupApiRequest, OrderGroupApiResponse> {

    private final OrderGroupRepository orderGroupRepository;

    private final UserRepository userRepository;

    @Override
    public Header<OrderGroupApiResponse> create(Header<OrderGroupApiRequest> request) {

        OrderGroupApiRequest body = request.getData();

        OrderGroup orderGroup = OrderGroup.builder()
                .status(body.getStatus())
                .orderType(body.getOrderType())
                .revAddress(body.getRevAddress())
                .revName(body.getRevName())
                .paymentType(body.getPaymentType())
                .totalPrice(body.getTotalPrice())
                .totalQuantity(body.getTotalQuantity())
                .orderAt(LocalDateTime.now())
                .user(userRepository.getOne(body.getUserId()))
                .build();
        OrderGroup newOrderGroup = orderGroupRepository.save(orderGroup);

        return Header.OK(response(newOrderGroup));
    }

    @Override
    public Header<OrderGroupApiResponse> read(Long id) {
        
        return orderGroupRepository.findById(id)
//                .map(this::response)                      // 1. this : ?????? ????????? > ?????? response?????? ???????????? ??????????????? ?????? ???
//                .map(orderGroup -> response(orderGroup))  // 2.
                .map(orderGroup -> {                        // 3.
                    return response(orderGroup);
                })
                .map(Header::OK)
                .orElseGet(()->Header.ERROR("????????? ??????"));
    }

    @Override
    public Header<OrderGroupApiResponse> update(Header<OrderGroupApiRequest> request) {

        OrderGroupApiRequest body = request.getData();

        return orderGroupRepository.findById(body.getId())
                .map(orderGroup -> {
                    orderGroup
                            .setStatus(body.getStatus())
                            .setOrderType(body.getOrderType())
                            .setRevAddress(body.getRevAddress())
                            .setRevName(body.getRevName())
                            .setPaymentType(body.getPaymentType())
                            .setTotalPrice(body.getTotalPrice())
                            .setTotalQuantity(body.getTotalQuantity())
                            .setOrderAt(body.getOrderAt())
                            .setArrivalDate(body.getArrivalDate())
                            .setUser(userRepository.getOne(body.getUserId()))
                            ;

                    return orderGroup;
                })
                .map(changeOrderGroup -> orderGroupRepository.save(changeOrderGroup))
                .map(newOderGroup -> response(newOderGroup))
                .map(Header::OK)
                .orElseGet(()->Header.ERROR("????????? ??????"));
    }

    @Override
    public Header delete(Long id) {
        
        return orderGroupRepository.findById(id)
                .map(orderGroup -> {
                    orderGroupRepository.delete(orderGroup);
                    return Header.OK();
                })
                .orElseGet(()->Header.ERROR("????????? ??????"));
    }

    public OrderGroupApiResponse response(OrderGroup orderGroup){

        OrderGroupApiResponse body = OrderGroupApiResponse.builder()
                .id(orderGroup.getId())
                .status(orderGroup.getStatus())
                .orderType(orderGroup.getOrderType())
                .revAddress(orderGroup.getRevAddress())
                .revName(orderGroup.getRevName())
                .paymentType(orderGroup.getPaymentType())
                .totalPrice(orderGroup.getTotalPrice())
                .totalQuantity(orderGroup.getTotalQuantity())
                .orderAt(orderGroup.getOrderAt())
                .arrivalDate(orderGroup.getArrivalDate())
                .userId(orderGroup.getUser().getId())
                .build();

        return body;
    }

    public Header<List<OrderGroupApiResponse>> search(Pageable pageable) {
        Page<OrderGroup> orderGroups = orderGroupRepository.findAll(pageable); // ?????? page??? ???????????? Category???????????? Page<Category> ????????? ??????
        List<OrderGroupApiResponse> orderGroupApiResponseList = orderGroups.stream()
                .map(orderGroup -> response(orderGroup))
                .collect(Collectors.toList());

        // page?????? ??????
        Pagination pagination = Pagination.builder()
                .totalPages(orderGroups.getTotalPages())                 // ?????? ????????? ???
                .totalElements(orderGroups.getTotalElements())           // ?????? elements ???
                .currentPage(orderGroups.getNumber())                    // ?????? ?????????
                .currentElements(orderGroups.getNumberOfElements())      // ?????? ???????????? ????????? data??? ???????????????
                .build();


        return Header.OK(orderGroupApiResponseList,pagination);
    }
}
