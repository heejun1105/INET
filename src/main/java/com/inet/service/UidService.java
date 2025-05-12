package com.inet.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import com.inet.entity.Uid;
import com.inet.repository.UidRepository;
import com.inet.entity.School;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UidService {
    
    private static final Logger log = LoggerFactory.getLogger(UidService.class);
    
    private final UidRepository uidRepository;
    
    @Autowired
    public UidService(UidRepository uidRepository) {
        this.uidRepository = uidRepository;
    }
    
    /**
     * 새로운 Uid 생성
     * @param cate 카테고리
     * @param idNumber ID 번호
     * @return 생성된 Uid 객체
     */
    public Uid createUid(String cate, Long idNumber) {
        log.info("Creating new Uid with cate: {}, idNumber: {}", cate, idNumber);
        
        Uid uid = new Uid();
        uid.setCate(cate);
        uid.setIdNumber(idNumber);
        
        return uidRepository.save(uid);
    }
    
    /**
     * 카테고리에 대한 다음 ID 번호 생성 및 저장
     * @param cate 카테고리
     * @return 생성된 Uid 객체
     */
    public Uid createNextUid(String cate) {
        log.info("Creating next Uid for cate: {}", cate);
        
        // 해당 카테고리의 최대 idNumber 조회
        Long nextIdNumber = uidRepository.findTopByCateOrderByIdNumberDesc(cate)
                .map(uid -> uid.getIdNumber() + 1)
                .orElse(1L); // 없으면 1부터 시작
        
        return createUid(cate, nextIdNumber);
    }
    
    /**
     * ID로 Uid 조회
     * @param uidId Uid ID
     * @return Uid 객체 (Optional)
     */
    public Optional<Uid> getUidById(Long uidId) {
        log.info("Getting Uid by id: {}", uidId);
        return uidRepository.findById(uidId);
    }
    
    /**
     * 카테고리로 Uid 목록 조회
     * @param cate 카테고리
     * @return Uid 목록
     */
    public List<Uid> getUidsByCate(String cate) {
        log.info("Getting Uids by cate: {}", cate);
        return uidRepository.findByCate(cate);
    }
    
    /**
     * 카테고리와 ID 번호로 Uid 조회
     * @param cate 카테고리
     * @param idNumber ID 번호
     * @return Uid 객체 (Optional)
     */
    public Optional<Uid> getUidByCateAndIdNumber(String cate, Long idNumber) {
        log.info("Getting Uid by cate: {} and idNumber: {}", cate, idNumber);
        return uidRepository.findByCateAndIdNumber(cate, idNumber);
    }
    
    /**
     * 모든 Uid 조회
     * @return 모든 Uid 목록
     */
    public List<Uid> getAllUids() {
        log.info("Getting all Uids");
        return uidRepository.findAll();
    }
    
    /**
     * Uid 업데이트
     * @param uid 업데이트할 Uid 객체
     * @return 업데이트된 Uid 객체
     */
    public Uid updateUid(Uid uid) {
        log.info("Updating Uid: {}", uid);
        
        if (!uidRepository.existsById(uid.getUidId())) {
            throw new IllegalArgumentException("Uid with ID " + uid.getUidId() + " does not exist");
        }
        
        return uidRepository.save(uid);
    }
    
    /**
     * ID로 Uid 삭제
     * @param uidId 삭제할 Uid ID
     */
    public void deleteUid(Long uidId) {
        log.info("Deleting Uid with id: {}", uidId);
        uidRepository.deleteById(uidId);
    }
    
    /**
     * 카테고리 존재 여부 확인
     * @param cate 카테고리
     * @return 존재 여부
     */
    public boolean existsByCate(String cate) {
        return uidRepository.existsByCate(cate);
    }
    
    /**
     * 카테고리의 마지막 ID 번호 조회
     * @param cate 카테고리
     * @return 마지막 ID 번호 (없으면 0)
     */
    public Long getLastIdNumber(String cate) {
        return uidRepository.findTopByCateOrderByIdNumberDesc(cate)
                .map(uid -> uid.getIdNumber())
                .orElse(0L);
    }

    /**
     * 카테고리와 학교에 대한 다음 ID 번호 생성 및 저장
     * @param cate 카테고리
     * @param school 학교
     * @return 생성된 Uid 객체
     */
    public Uid createNextUidWithSchool(String cate, School school) {
        System.out.println("Creating next Uid for cate: " + cate + " and school: " + school.getSchoolName());
        
        // 해당 카테고리와 학교의 최대 idNumber 조회
        Long nextIdNumber = uidRepository.findTopBySchoolAndCateOrderByIdNumberDesc(school, cate)
                .map(uid -> uid.getIdNumber() + 1)
                .orElse(1L); // 없으면 1부터 시작
        
        Uid uid = new Uid();
        uid.setCate(cate);
        uid.setIdNumber(nextIdNumber);
        uid.setSchool(school);
        
        return uidRepository.save(uid);
    }

    /**
     * 카테고리, ID 번호, 학교로 Uid 생성
     * @param cate 카테고리
     * @param idNumber ID 번호
     * @param school 학교
     * @return 생성된 Uid 객체
     */
    public Uid createUidWithSchool(String cate, Long idNumber, School school) {
        System.out.println("Creating Uid with cate: " + cate + ", idNumber: " + idNumber + ", school: " + school.getSchoolName());
        
        Uid uid = new Uid();
        uid.setCate(cate);
        uid.setIdNumber(idNumber);
        uid.setSchool(school);
        
        return uidRepository.save(uid);
    }

    /**
     * 학교별 카테고리의 마지막 ID 번호 조회
     * @param cate 카테고리
     * @param school 학교
     * @return 마지막 ID 번호 (없으면 0)
     */
    public Long getLastIdNumberBySchool(String cate, School school) {
        return uidRepository.findTopBySchoolAndCateOrderByIdNumberDesc(school, cate)
                .map(uid -> uid.getIdNumber())
                .orElse(0L);
    }

    /**
     * 학교별 Uid 목록 조회
     * @param school 학교
     * @return Uid 목록
     */
    public List<Uid> getUidsBySchool(School school) {
        return uidRepository.findBySchool(school);
    }

    /**
     * 학교와 카테고리로 Uid 목록 조회
     * @param school 학교
     * @param cate 카테고리
     * @return Uid 목록
     */
    public List<Uid> getUidsBySchoolAndCate(School school, String cate) {
        return uidRepository.findBySchoolAndCate(school, cate);
    }

    /**
     * 학교, 카테고리, ID 번호로 Uid 조회
     * @param school 학교
     * @param cate 카테고리
     * @param idNumber ID 번호
     * @return Uid 객체 (Optional)
     */
    public Optional<Uid> findBySchoolAndCateAndIdNumber(School school, String cate, Long idNumber) {
        return uidRepository.findBySchoolAndCateAndIdNumber(school, cate, idNumber);
    }
} 