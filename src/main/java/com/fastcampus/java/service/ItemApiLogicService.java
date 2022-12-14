package com.fastcampus.java.service;

import com.fastcampus.java.controller.CrudController;
import com.fastcampus.java.ifs.CrudInterface;
import com.fastcampus.java.model.entity.Category;
import com.fastcampus.java.model.entity.Item;
import com.fastcampus.java.model.network.Header;
import com.fastcampus.java.model.network.Pagination;
import com.fastcampus.java.model.network.request.ItemApiRequest;
import com.fastcampus.java.model.network.response.CategoryApiResponse;
import com.fastcampus.java.model.network.response.ItemApiResponse;
import com.fastcampus.java.repository.ItemRepository;
import com.fastcampus.java.repository.PartnerRepository;
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
public class ItemApiLogicService implements CrudInterface<ItemApiRequest, ItemApiResponse> {

    private final PartnerRepository partnerRepository;

    private final ItemRepository itemRepository;

    @Override
    public Header<ItemApiResponse> create(Header<ItemApiRequest> request) {

        ItemApiRequest body = request.getData();

        Item item = Item.builder()
                .status(body.getStatus())
                .name(body.getName())
                .title(body.getTitle())
                .content(body.getContent())
                .price(body.getPrice())
                .brandName(body.getBrandName())
                .registeredAt(LocalDateTime.now())
                .partner(partnerRepository.getOne(body.getPartnerId()))
                .build();

        Item newItem = itemRepository.save(item);

        return Header.OK(response(newItem));
    }

//    @Override
//    public Header<ItemApiResponse> read(Long id) {
//        return itemRepository.findById(id)
//                .map(item -> response(item))
//                .orElseGet(()-> Header.ERROR("????????? ??????"));
//    }

    @Override
    public Header<ItemApiResponse> read(Long id) {

        Optional<Item> optional = itemRepository.findById(id);

        return itemRepository.findById(id)
                .map(item -> response(item))
                .map(Header::OK)
                .orElseGet(()-> Header.ERROR("????????? ??????"));
    }

    @Override
    public Header<ItemApiResponse> update(Header<ItemApiRequest> request) {
        
        ItemApiRequest body = request.getData();
        
        return itemRepository.findById(body.getId())
                .map(entityItem -> {
                    entityItem
                            .setStatus(body.getStatus())
                            .setName(body.getName())
                            .setTitle(body.getTitle())
                            .setContent(body.getContent())
                            .setPrice(body.getPrice())
                            .setBrandName(body.getBrandName())
                            .setRegisteredAt(body.getRegisteredAt())
                            .setUnregisteredAt(body.getUnregisteredAt())
                            ;

                    return entityItem;
                })
                .map(newEntityItem -> itemRepository.save(newEntityItem))
                .map(item -> response(item))
                .map(Header::OK)
                .orElseGet(()->Header.ERROR("????????? ??????"));
    }

    @Override
    public Header delete(Long id) {
        return itemRepository.findById(id)
                .map(item->{
                    itemRepository.delete(item);
                    return Header.OK();
                })
                .orElseGet(()->Header.ERROR("????????? ??????"));
    }

    public ItemApiResponse response(Item item){

        ItemApiResponse body = ItemApiResponse.builder()
                .id(item.getId())
                .status(item.getStatus())
                .name(item.getName())
                .title(item.getTitle())
                .content(item.getContent())
                .price(item.getPrice())
                .brandName(item.getBrandName())
                .registeredAt(item.getRegisteredAt())
                .unregisteredAt(item.getUnregisteredAt())
                .partnerId(item.getPartner().getId())
                .build();

        return body;
    }

    public Header<List<ItemApiResponse>> search(Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable); // ?????? page??? ???????????? Category???????????? Page<Category> ????????? ??????
        List<ItemApiResponse> itemApiResponseList = items.stream()
                .map(item -> response(item))
                .collect(Collectors.toList());

        // page?????? ??????
        Pagination pagination = Pagination.builder()
                .totalPages(items.getTotalPages())                 // ?????? ????????? ???
                .totalElements(items.getTotalElements())           // ?????? elements ???
                .currentPage(items.getNumber())                    // ?????? ?????????
                .currentElements(items.getNumberOfElements())      // ?????? ???????????? ????????? data??? ???????????????
                .build();


        return Header.OK(itemApiResponseList,pagination);
    }
}
