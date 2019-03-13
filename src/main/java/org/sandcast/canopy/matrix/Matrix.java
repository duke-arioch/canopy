package org.sandcast.canopy.matrix;

import javafx.util.Pair;

import java.util.function.BiPredicate;
import java.util.function.Function;

abstract public class Matrix<T> {

    public static <S> Matrix of(S[][] s) {
        return new Matrix<S>() {
            private S[][] inner = s;

            @Override
            public S get(int x, int y) {
                return inner[x][y];
            }

            @Override
            public int size() {
                return inner.length;
            }
        };
    }

    public Matrix<T> rotate() {

        Matrix matrix = new Matrix<T>() {
            Matrix<T> inner = Matrix.this;

            @Override
            public T get(int x, int y) {
                return inner.get(size() - y - 1, x);
            }

            @Override
            public int size() {
                return inner.size();
            }

        };
        return matrix;
    }

    public <S> Matrix<S> view(Function<T, S> transformation) {

        Matrix matrix = new Matrix<S>() {
            Matrix<T> inner = Matrix.this;

            @Override
            public S get(int x, int y) {
                return transformation.apply(inner.get(x, y));
            }

            @Override
            public int size() {
                return inner.size();
            }

        };
        return matrix;
    }

    public Matrix<T> transform(Function<Pair<Integer, Integer>, Pair<Integer, Integer>> function) {
        Matrix matrix = new Matrix<T>() {
            Matrix<T> inner = Matrix.this;

            @Override
            public T get(int x, int y) {
                Pair<Integer, Integer> temp = function.apply(new Pair<>(x, y));
                return inner.get(temp.getKey(), temp.getValue());
            }

            @Override
            public int size() {
                return inner.size();
            }
        };
        return matrix;
    }

    abstract public T get(int x, int y);

    abstract public int size();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        for (int x = 0; x < size(); x++) {
            for (int y = 0; y < size(); y++) {
                builder.append("[")
                        .append(get(x, y))
                        .append("] ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public <S> boolean contains(Matrix<S> other, BiPredicate<T, S> equalityFunction) {
        if (other.size() > this.size()) return false;
        final int requiredMatches = other.size() * other.size();
        final int sizeDifference = this.size() - other.size();
        int matches = 0;
        for (int offsety = 0; offsety <= sizeDifference && matches < requiredMatches; offsety++) {
            for (int offsetx = 0; offsetx <= sizeDifference && matches < requiredMatches; offsetx++) {
                matches = 0;
//                System.out.println("matrix check offset = " + offsetx + ", " + offsety);
                for (int y = 0; y < other.size() && matches < requiredMatches; y++) {
                    for (int x = 0; x < other.size() && matches < requiredMatches; x++) {
                        final S recipeValue = other.get(x, y);
                        final T blockValue = get(x + offsety, y + offsetx);
                        if (equalityFunction.test(blockValue, recipeValue)) matches++;
                        else break;
                    }
                }
            }
        }
//        System.out.println("matrix check matches " + matches + " of " + requiredMatches);
        return matches == requiredMatches;
    }

    public <S> boolean containsRotated(Matrix<S> ingredients, BiPredicate<T, S> equalityFunction) {
        boolean match = false;
        for (int i = 0; i < 4; i++) {
//            System.out.println("rotation check angle: " + i);
//            System.out.println(this.toString());
//            System.out.println(ingredients.toString());
            if (this.contains(ingredients, equalityFunction)) {
                match = true;
                break;
            } else {
                ingredients = ingredients.rotate();
            }
        }
        return match;
    }
}
