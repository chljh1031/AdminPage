package com.fastcampus.java.service;

import com.fastcampus.java.ifs.CrudInterface;
import com.fastcampus.java.model.entity.Category;
import com.fastcampus.java.model.entity.Partner;
import com.fastcampus.java.model.network.Header;
import com.fastcampus.java.model.network.Pagination;
import com.fastcampus.java.model.network.request.PartnerApiRequest;
import com.fastcampus.java.model.network.response.CategoryApiResponse;
import com.fastcampus.java.model.network.response.PartnerApiResponse;
import com.fastcampus.java.repository.CategoryRepository;
import com.fastcampus.java.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartnerApiLogicService implements CrudInterface<PartnerApiRequest, PartnerApiResponse> {

    private final PartnerRepository partnerRepository;

    private final CategoryRepository categoryRepository;

    @Override
    public Header<PartnerApiResponse> create(Header<PartnerApiRequest> request) {

        PartnerApiRequest body = request.getData();

        Partner partner = Partner.builder()
                .name(body.getName())
                .status(body.getStatus())
                .address(body.getAddress())
                .callCenter(body.getCallCenter())
                .partnerNumber(body.getPartnerNumber())
                .businessNumber(body.getBusinessNumber())
                .ceoName(body.getCeoName())
                .registeredAt(body.getRegisteredAt())
                .unregisteredAt(body.getUnregisteredAt())
                .category(categoryRepository.getOne(body.getCategoryId()))
                .build();

        Partner newPartner = partnerRepository.save(partner);

        return Header.OK(response(newPartner));
    }

    @Override
    public Header<PartnerApiResponse> read(Long id) {
        return partnerRepository.findById(id)
                .map(this::response)
                .map(Header::OK)
                .orElseGet(()->Header.ERROR("????????? ??????"));
    }

    @Override
    public Header<PartnerApiResponse> update(Header<PartnerApiRequest> request) {
        
        PartnerApiRequest body = request.getData();
        
        return partnerRepository.findById(body.getId())
                .map(entityPartner -> {
                    entityPartner
                            .setName(body.getName())
                            .setStatus(body.getStatus())
                            .setAddress(body.getAddress())
                            .setCallCenter(body.getCallCenter())
                            .setPartnerNumber(body.getPartnerNumber())
                            .setBusinessNumber(body.getBusinessNumber())
                            .setCeoName(body.getCeoName())
                            .setRegisteredAt(body.getRegisteredAt())
                            .setUnregisteredAt(body.getUnregisteredAt())
                            .setCategory(categoryRepository.getOne(body.getCategoryId()))
                            ;
                    return entityPartner;
                })
                .map(newEntityPartner -> partnerRepository.save(newEntityPartner))
                .map(partner -> response(partner))
                .map(Header::OK)
                .orElseGet(()->Header.ERROR("????????? ??????"));
    }

    @Override
    public Header delete(Long id) {
        return partnerRepository.findById(id)
                .map(partner -> {
                    partnerRepository.delete(partner);
                    return Header.OK();
                })
                .orElseGet(()->Header.ERROR("????????? ??????"));
    }

    private PartnerApiResponse response(Partner partner){

        PartnerApiResponse body = PartnerApiResponse.builder()
                .id(partner.getId())
                .name(partner.getName())
                .status(partner.getStatus())
                .address(partner.getAddress())
                .callCenter(partner.getCallCenter())
                .partnerNumber(partner.getPartnerNumber())
                .businessNumber(partner.getBusinessNumber())
                .ceoName(partner.getCeoName())
                .registeredAt(partner.getRegisteredAt())
                .unregisteredAt(partner.getUnregisteredAt())
                .categoryId(partner.getCategory().getId())
                .build();

        return body;
    }

    public Header<List<PartnerApiResponse>> search(Pageable pageable) {
        Page<Partner> partners = partnerRepository.findAll(pageable); // ?????? page??? ???????????? Category???????????? Page<Category> ????????? ??????
        List<PartnerApiResponse> partnerApiResponseList = partners.stream()
                .map(partner -> response(partner))
                .collect(Collectors.toList());

        // page?????? ??????
        Pagination pagination = Pagination.builder()
                .totalPages(partners.getTotalPages())                 // ?????? ????????? ???
                .totalElements(partners.getTotalElements())           // ?????? elements ???
                .currentPage(partners.getNumber())                    // ?????? ?????????
                .currentElements(partners.getNumberOfElements())      // ?????? ???????????? ????????? data??? ???????????????
                .build();


        return Header.OK(partnerApiResponseList,pagination);
    }
}
