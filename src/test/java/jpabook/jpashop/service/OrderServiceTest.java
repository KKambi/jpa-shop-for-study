package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@Transactional
@SpringBootTest
public class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @DisplayName("상품 주문")
    @Test
    void 상품_주문() {
        // given
        Member member = createMember("회원1", new Address("서울", "경기", "123-123"));
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // then
        Order foundOrder = orderRepository.findOne(orderId);
        then(foundOrder.getStatus()).isEqualTo(OrderStatus.ORDER);
        then(foundOrder.getOrderItems().size()).isEqualTo(1);
        then(foundOrder.getTotalPrice()).isEqualTo(10000 * orderCount);
        then(book.getStockQuantity()).isEqualTo(10 - orderCount);
    }



    @DisplayName("주문 취소")
    @Test
    void 주문_취소() {
        // given
        Member member = createMember("회원1", new Address("서울", "경기", "123-123"));
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order foundOrder = orderRepository.findOne(orderId);
        then(foundOrder.getStatus()).isEqualTo(OrderStatus.CANCEL);
        then(book.getStockQuantity()).isEqualTo(10);
    }

    @DisplayName("상품주문_재고수량초과")
    @Test
    void 상품주문_재고수량초과() {
        // given
        Member member = createMember("회원1", new Address("서울", "경기", "123-123"));
        Book book = createBook("시골 JPA", 10000, 10);

        int orderCount = 11;

        // when
        // then
        assertThrows(NotEnoughStockException.class, () -> {
            Long orderId = orderService.order(member.getId(), book.getId(), orderCount);
        });
    }

    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String name, Address address) {
        Member member = new Member();
        member.setName(name);
        member.setAddress(address);
        em.persist(member);
        return member;
    }
}